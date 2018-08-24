package com.xeomar.acorn;

import com.xeomar.product.ProductCard;
import com.xeomar.util.LogUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class Program {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private ProductCard card;

	public Program() throws IOException {
		this.card = new ProductCard().init( getClass() );
	}

	public static void main( String[] commands ) {
		try {
			new Program().run( commands );
		} catch( IOException exception ) {
			log.error( "Error initializing program", exception );
		}
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
		StringBuilder bar = new StringBuilder();
		for( int index = 0; index < counters.length; index++ ) {
			bar.append( "=====" );
		}
		System.out.println( "|" + bar + "|" );

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
