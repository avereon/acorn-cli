package com.avereon.acorn;

import com.avereon.product.ProductCard;
import com.avereon.util.Log;

public class Program {

	private static final System.Logger log = Log.get();

	private final ProductCard card;

	public Program() {
		this.card = new ProductCard().card( getClass() );
	}

	public static void main( String[] commands ) {
		new Program().run( commands );
	}

	public ProductCard getCard() {
		return card;
	}

	public void run( String[] commands ) {
		printHeader( card );
		try {
			long score = runTests( new XorShiftCounter(), new RandomCounter(), new MultiplyCounter() );

			System.out.println( "Squirrel count: " + getCoreCount() );
			System.out.println( "Acorn score:    " + (score / 100) );
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		}
		System.exit( 0 );
	}

	public int getCoreCount() {
		return Runtime.getRuntime().availableProcessors();
	}

	public long runTests( Counter... counters ) {
		System.out.println( "|" + "=====".repeat( counters.length ) + "|" );
		System.out.print( "|" );
		long count = 0;
		for( Counter counter : counters ) {
			try {
				count += runTest( counter ).getAvg();
			} catch( InterruptedException exception ) {
				exception.printStackTrace( System.err );
			}
		}
		System.out.println( "|" );

		return count / counters.length;
	}

	private void printHeader( ProductCard card ) {
		System.out.println( card.getName() + " " + card.getVersion() );
	}

	private Statistics runTest( Counter counter ) throws InterruptedException {
		int valueCount = 10;
		int time = 1000 / valueCount;
		int iterationLimit = 5;
		int iterationCount = 0;

		Statistics lastStats;
		Statistics bestStats = null;
		long[] values = new long[ valueCount ];
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
