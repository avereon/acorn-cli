package com.avereon.acorn;

import com.avereon.util.Log;
import com.avereon.xenon.task.Task;

public class AcornChecker extends Task<Long> {

	private static final System.Logger log = Log.get();

	private static final int ITERATION_LIMIT = 5;

	private int step;

	private int steps;

	@Override
	public Long call() throws Exception {
		try {
			return runTests( new XorShiftCounter(), new RandomCounter(), new MultiplyCounter() );
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

	public long runTests( Counter... counters ) throws InterruptedException {
		setTotal( this.steps = counters.length * ITERATION_LIMIT );
		this.step = 0;

		long count = 0;
		for( Counter counter : counters ) {
			count += runTest( counter ).getAvg();
		}

		return count / counters.length / 100;
	}

	private Statistics runTest( Counter counter ) throws InterruptedException {
		int valueCount = 10;
		int time = 1000 / valueCount;
		int iterationCount = 0;

		Statistics lastStats;
		Statistics bestStats = null;
		long[] values = new long[ valueCount ];
		do {
			for( int index = 0; index < valueCount; index++ ) {
				values[ index ] = counter.runFor( time );
			}
			lastStats = new Statistics( values, time );
			if( bestStats == null || lastStats.getJitter() < bestStats.getJitter() ) bestStats = lastStats;
			setProgress( ++step );
			iterationCount++;
		} while( iterationCount < ITERATION_LIMIT );

		return bestStats;
	}

}
