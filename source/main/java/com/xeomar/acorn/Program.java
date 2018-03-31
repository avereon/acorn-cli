package com.xeomar.acorn;

public class Program {

	public static void main( String[] commands ) {
		new Program().run( commands );
	}

	public void run( String[] commands ) {
		System.out.println( "Acorn CPU check" );
		try {
			long score = runTests( new XorShiftCounter(), new RandomCounter(), new AddCounter() );

			System.out.println( "Squirrel (core) count: " + getCoreCount() );
			System.out.println( "Acorns per squirrel:   " + ( score / 100 ) );
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		}
		System.exit(0);
	}

	public int getCoreCount() {
		return Runtime.getRuntime().availableProcessors();
	}

	public long runTests( Counter... counters ) {
		StringBuilder bar = new StringBuilder();
		for( int index =0; index < counters.length; index++ ) {
			bar.append( "=====");
		}
		System.out.println( "|" + bar + "|");

		System.out.print( "|" );
		long count = 0;
		for( Counter counter : counters ) {
			try {
				count += runTest( counter ).getAvg();
			} catch( InterruptedException exception ) {
				exception.printStackTrace( System.err );
			}
		}
		System.out.println( "|");

		return count / counters.length;
	}

	public Statistics runTest( Counter counter ) throws InterruptedException {
		int valueCount = 10;
		int time = 1000 / valueCount;
		int iterationLimit = 5;
		int iterationCount = 0;

		long[] values = new long[ valueCount ];
		Statistics bestStats = null;
		Statistics lastStats;
		do {
			System.out.print( '-' );
			for( int index = 0; index < valueCount; index++ ) {
				values[ index ] = counter.runFor( time );
			}
			lastStats = new Statistics( values, time );
			if( bestStats == null || lastStats.getJitter() < bestStats.getJitter() ) bestStats = lastStats;
			iterationCount++;
		} while( iterationCount < iterationLimit );

		return bestStats;
	}

}
