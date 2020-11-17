package com.avereon.acorn;

import com.avereon.acorn.test.LoadTest;
import com.avereon.util.Log;
import com.avereon.xenon.task.Task;

public class AcornChecker extends Task<Long> {

	private static final System.Logger log = Log.get();

	private static final int ITERATION_LIMIT = 10;

	private static final int STEPS_PER_ITERATION = 10;

	private int step;

	private int steps;

	@Override
	public Long call() throws Exception {
		try {
			return runTests( new LoadTest() );
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

	public long runTests( Runnable... tests ) throws InterruptedException {
		setTotal( this.steps = tests.length * ITERATION_LIMIT * STEPS_PER_ITERATION );
		this.step = 0;

		long count = 0;
		for( Runnable test : tests ) {
			count += runTest( new Counter( test ) ).getAvg();
		}

		return count / tests.length / 100;
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
