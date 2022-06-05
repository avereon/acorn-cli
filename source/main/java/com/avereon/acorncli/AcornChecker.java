package com.avereon.acorncli;

import com.avereon.acorncli.test.LoadTest;
import lombok.CustomLog;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The way this works is that the tests are as many times as possible in a small
 * period of time called a sample. Multiple samples are taken and the samples
 * with the smallest amount of jitter are added to an iteration.
 * <p>
 * multiple small periods of time, a step, and grouped into iterations. The step
 * with the least amount of jitter is chosen to represent the iteration. Several
 * iterations are run and the values of all iterations are averaged together.
 * This process seems to provide the most consistent results across multiple
 * runs.
 */
@CustomLog
public class AcornChecker implements Callable<Long> {

	public static final int SAMPLE_TIME_MS = 100;

	public static final int ITERATION_COUNT = 5;

	public static final int BLOCKS_PER_ITERATION = 10;

	public static final int SAMPLES_PER_BLOCK = 10;

	private int steps;

	private final Set<Consumer<Integer>> listeners;

	private final Runnable[] tests;

	private final int coreCount;

	public AcornChecker() {
		this( getAvailableCoreCount(), new LoadTest() );
	}

	public AcornChecker( Runnable... tests ) {
		this( getAvailableCoreCount(), tests );
	}

	public AcornChecker( int coreCount, Runnable... tests ) {
		this.coreCount = coreCount;
		this.tests = tests;
		this.listeners = new CopyOnWriteArraySet<>();
		setTotal( this.steps = tests.length * ITERATION_COUNT );
	}

	@Override
	public Long call() throws Exception {
		try {
			return runTests( tests );
		} catch( InterruptedException ignore ) {
			return 0L;
		}
	}

	public static int getAvailableCoreCount() {
		return Runtime.getRuntime().availableProcessors();
	}

	public int getCoreCount() {
		return coreCount;
	}

	public int getStepCount() {
		return steps;
	}

	public void setTotal( int count ) {
		this.steps = count;
	}

	public void setProgress( int progress ) {
		fireEvent( progress );
	}

	public long runTests( Runnable... tests ) throws ExecutionException, InterruptedException {
		long count = 0;

		final int coreCount = getCoreCount();
		ExecutorService executor = Executors.newFixedThreadPool( coreCount );

		// Start all the work
		Set<Iteration> iterations = new HashSet<>();
		Set<Future<Sample>> futures = new HashSet<>();
		for( int coreIndex = 0; coreIndex < coreCount; coreIndex++ ) {
			for( Runnable test : tests ) {
				Iteration iteration = new Iteration( test );
				futures.addAll( executor.invokeAll( iteration.getBlocks() ) );
				iterations.add( iteration );
			}
		}

		// Wait for all the work
		for( Future<Sample> future : futures ) {
			future.get();
		}

		count += iterations.stream().mapToLong( i -> i.getStatistics().getAvg() ).sum();

		return count / tests.length / 100 / getCoreCount();
	}

	public void addListener( Consumer<Integer> runnable ) {
		listeners.add( runnable );
	}

	public void removeListener( Consumer<Integer> runnable ) {
		listeners.remove( runnable );
	}

	private int count;

	//	private Statistics runTest2( Counter2 counter ) throws InterruptedException {
	//		Statistics best = null;
	//
	//		for( int iterationIndex = 0; iterationIndex < ITERATION_COUNT; iterationIndex++ ) {
	//			Statistics stats = new Statistics( SAMPLES_PER_ITERATION );
	//			for( int sampleIndex = 0; sampleIndex < SAMPLES_PER_ITERATION; sampleIndex++ ) {
	//				stats.add( counter.waitFor(), counter.getNanoTime() );
	//				count++;
	//			}
	//			stats.process();
	//			if( best == null || stats.getJitter() < best.getJitter() ) best = stats;
	//			//if( best == null || stats.getMax() < best.getMax() ) best = stats;
	//
	//			// This logic takes into consideration how many cores are being used
	//			if( count % getCoreCount() == 0 ) setProgress( count / getCoreCount() );
	//		}
	//
	//		return best;
	//	}

	private Statistics runTest( Counter counter ) throws InterruptedException {
		Statistics best = null;

		for( int iterationIndex = 0; iterationIndex < ITERATION_COUNT; iterationIndex++ ) {
			Statistics stats = new Statistics( BLOCKS_PER_ITERATION );
			for( int sampleIndex = 0; sampleIndex < BLOCKS_PER_ITERATION; sampleIndex++ ) {
				counter.runFor( SAMPLE_TIME_MS );
				stats.add( counter.getCount(), counter.getNanoTime() );
				count++;
			}
			stats.process();
			if( best == null || stats.getJitter() < best.getJitter() ) best = stats;
			//if( best == null || stats.getMax() < best.getMax() ) best = stats;

			// This logic takes into consideration how many cores are being used
			if( count % getCoreCount() == 0 ) setProgress( count / getCoreCount() );
		}

		return best;
	}

	private void fireEvent( int step ) {
		for( Consumer<Integer> listener : new HashSet<>( listeners ) ) {
			listener.accept( step );
		}
	}

}

class Iteration {

	private final Set<Block> blocks;

	public Iteration( Runnable test ) {
		blocks = new HashSet<>();
		for( int iterationIndex = 0; iterationIndex < AcornChecker.BLOCKS_PER_ITERATION; iterationIndex++ ) {
			blocks.add( new Block( test ) );
		}
	}

	public Set<Sample> getBlocks() {
		return blocks.stream().flatMap( b -> b.getSamples().stream() ).collect( Collectors.toSet() );
	}

	public Statistics getStatistics() {
		Statistics best;

		// Find the "best" sample from the block
		best = blocks.iterator().next().collect();

		return best;
	}

}

class Block {

	private final Set<Sampler> samples;

	// FIXME This should run samples in coreCount groups

	public Block( Runnable test ) {
		samples = new HashSet<>();
		for( int iterationIndex = 0; iterationIndex < AcornChecker.SAMPLES_PER_BLOCK; iterationIndex++ ) {
			samples.add( new Sampler( AcornChecker.SAMPLE_TIME_MS, test ) );
		}
	}

	public Set<? extends Sample> getSamples() {
		return samples;
	}

	public Statistics collect() {
		samples.forEach( System.out::println );

		Statistics stats = new Statistics( AcornChecker.SAMPLES_PER_BLOCK );
		samples.forEach( s -> stats.add( s.getCount(), s.getNanos() ) );
		stats.process();
		return stats;
	}

}
