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

	public static final int ITERATIONS_PER_TEST = 5;

	public static final int BLOCKS_PER_ITERATION = 10;

	private int steps;

	private final Set<Consumer<Integer>> listeners;

	private final Runnable[] tests;

	private final int coreCount;

	private final ExecutorService executor;

	public AcornChecker() {
		this( getAvailableCoreCount(), new LoadTest() );
	}

	public AcornChecker( Runnable... tests ) {
		this( getAvailableCoreCount(), tests );
	}

	public AcornChecker( int coreCount, Runnable... tests ) {
		this.tests = tests;
		this.executor = Executors.newFixedThreadPool( this.coreCount = coreCount );
		this.listeners = new CopyOnWriteArraySet<>();
		setTotal( this.steps = tests.length * ITERATIONS_PER_TEST );
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

		// Start all the work
		Set<Iteration> iterations = new HashSet<>();
		for( int testIndex = 0; testIndex < tests.length; testIndex++ ) {
			for( int iterationIndex = 0; iterationIndex < ITERATIONS_PER_TEST; iterationIndex++ ) {
				Iteration iteration = new Iteration( tests[ testIndex ] );
				iterations.add( iteration );
				iteration.runBlocks();
				setProgress( testIndex * tests.length + iterationIndex );
			}
		}

		count += iterations.stream().mapToLong( i -> i.getStatistics().getAvg() ).sum();

		return count / iterations.size();
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

	//	private Statistics runTest( Counter counter ) throws InterruptedException {
	//		Statistics best = null;
	//
	//		for( int iterationIndex = 0; iterationIndex < ITERATION_COUNT; iterationIndex++ ) {
	//			Statistics stats = new Statistics( BLOCKS_PER_ITERATION );
	//			for( int sampleIndex = 0; sampleIndex < BLOCKS_PER_ITERATION; sampleIndex++ ) {
	//				counter.runFor( SAMPLE_TIME_MS );
	//				stats.add( counter.getCount(), counter.getNanoTime() );
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

	private void fireEvent( int step ) {
		for( Consumer<Integer> listener : new HashSet<>( listeners ) ) {
			listener.accept( step );
		}
	}

	private class Iteration {

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

		public void runBlocks() {
			blocks.forEach( Block::run );
		}

		public Statistics getStatistics() {
			Statistics best = null;

			// Find the "best" sample from the block
			for( Block block : blocks ) {
				if( best == null || block.stats().getJitter() < best.getJitter() ) best = block.stats();
			}
			System.out.println( "best=" + best );

			return best;
		}

	}

	private class Block implements Runnable {

		private final Set<Sampler> samples;

		private final int sampleCount;

		private final Runnable test;

		private final Statistics stats;

		public Block( Runnable test ) {
			this.test = test;
			this.samples = new HashSet<>();
			this.sampleCount = AcornChecker.this.getCoreCount();
			this.stats = new Statistics( sampleCount );
		}

		public Set<? extends Sample> getSamples() {
			return samples;
		}

		@Override
		public void run() {
			// Submit one sample per CPU
			for( int iterationIndex = 0; iterationIndex < sampleCount; iterationIndex++ ) {
				samples.add( new Sampler( AcornChecker.SAMPLE_TIME_MS, test ) );
			}

			try {
				//System.out.println( "Start samples..." );
				executor.invokeAll( samples );
			} catch( InterruptedException exception ) {
				exception.printStackTrace();
			}

			// Wait for all samples to complete and collect stats
			samples.forEach( s -> {
				try {
					s.waitFor();
					stats.add( s.getCount(), s.getNanos() );
				} catch( InterruptedException exception ) {
					exception.printStackTrace();
				}
			} );

			// Process the stats
			stats.process();
		}

		public Statistics stats() {
			return stats;
		}

	}

}
