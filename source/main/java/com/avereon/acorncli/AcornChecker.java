package com.avereon.acorncli;

import com.avereon.acorncli.test.LoadTest;
import lombok.CustomLog;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

@CustomLog
public class AcornChecker implements Callable<Long> {

	private static final int ITERATION_LIMIT = 10;

	private static final int STEPS_PER_ITERATION = 10;

	private int step;

	private int steps;

	private final Set<Consumer<Integer>> listeners;

	private final Runnable[] tests;

	public AcornChecker() {
		this( new LoadTest() );
	}

	public AcornChecker( Runnable... tests ) {
		listeners = new CopyOnWriteArraySet<>();
		this.tests = tests;
		setTotal( this.steps = tests.length * ITERATION_LIMIT * STEPS_PER_ITERATION );
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

	public long runTests( Runnable... tests ) throws InterruptedException {
		this.step = 0;

		long count = 0;
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

	private void fireEvent( int step ) {
		for( Consumer<Integer> listener : new HashSet<>( listeners ) ) {
			listener.accept( step );
		}
	}

	private Statistics runTest( Counter counter ) throws InterruptedException {
		int time = 100;
		int valueCount = 10;
		int iterationLimit = ITERATION_LIMIT * STEPS_PER_ITERATION;
		int iterationCount = 0;

		Statistics bestStats = null;
		int count = 0;
		Statistics stats = new Statistics( valueCount );
		do {
			counter.runFor( time );
			stats.add( counter.getCount(), counter.getTime() );
			count++;
			if( count >= valueCount ) {
				stats.process();
				if( bestStats == null || stats.getJitter() < bestStats.getJitter() ) bestStats = stats;
				count = 0;
				stats = new Statistics( valueCount );
			}
			setProgress( ++step );
			iterationCount++;
		} while( iterationCount < iterationLimit );

		return bestStats;
	}

}
