package com.avereon.acorncli;

import com.avereon.acorncli.test.LoadTest;
import lombok.CustomLog;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

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

	private static final int SAMPLE_TIME = 100;

	private static final int ITERATION_COUNT = 5;

	private static final int SAMPLES_PER_ITERATION = 10;

	private int steps;

	private final Set<Consumer<Integer>> listeners;

	private final Runnable[] tests;

	public AcornChecker() {
		this( new LoadTest() );
	}

	public AcornChecker( Runnable... tests ) {
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

	public int getCoreCount() {
		return Runtime.getRuntime().availableProcessors();
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

//		// Start all the work
//		Set<Future<Statistics>> futures = new HashSet<>();
//		for( int coreIndex = 0; coreIndex < coreCount; coreIndex++ ) {
//			for( Runnable test : tests ) {
//				futures.add( executor.submit( () -> runTest( new Counter( test ) ) ) );
//			}
//		}
//
//		// Wait for all the work
//		for( Future<Statistics> future : futures ) {
//			count += future.get().getAvg();
//		}

		// Single thread
				for( Runnable test : tests ) {
					count += runTest( new Counter( test ) ).getAvg();
				}

		return count / tests.length / 100;
	}

	public void addListener( Consumer<Integer> runnable ) {
		listeners.add( runnable );
	}

	public void removeListener( Consumer<Integer> runnable ) {
		listeners.remove( runnable );
	}

	private int count;

	private Statistics runTest( Counter counter ) throws InterruptedException {
		Statistics best = null;

		for( int iterationIndex = 0; iterationIndex < ITERATION_COUNT; iterationIndex++ ) {
			Statistics stats = new Statistics( SAMPLES_PER_ITERATION );
			for( int sampleIndex = 0; sampleIndex < SAMPLES_PER_ITERATION; sampleIndex++ ) {
				counter.runFor( SAMPLE_TIME );
				stats.add( counter.getCount(), counter.getTime() );
				count++;
			}
			stats.process();
			if( best == null || stats.getJitter() < best.getJitter() ) best = stats;

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
