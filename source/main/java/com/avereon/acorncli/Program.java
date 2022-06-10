package com.avereon.acorncli;

import com.avereon.acorncli.test.HashTest;
import com.avereon.product.ProductCard;
import com.avereon.util.TextUtil;
import lombok.CustomLog;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@CustomLog
public class Program {

	private final ProductCard card;

	public Program() {
		this.card = ProductCard.info( getClass() );
	}

	public static void main( String[] commands ) {
		new Program().run( commands );
	}

	public ProductCard getCard() {
		return card;
	}

	public void run( String[] commands ) {
		printHeader( card );

		AcornMonitor monitorAll = new AcornMonitor( Runtime.getRuntime().availableProcessors(), new HashTest() );
		AcornMonitor monitorOne = new AcornMonitor( 1, new HashTest() );

		int progressUnits = 50;
		int steps = (int)(monitorAll.getTotal() + monitorOne.getTotal());

		AtomicLong priorIncrementSteps = new AtomicLong( 0 );
		AtomicInteger priorProgressUnits = new AtomicInteger( 0 );
		AtomicLong monitorOffset = new AtomicLong( 0 );

		// Progress bar
		System.out.println( "|" + "=".repeat( progressUnits ) + "|" );
		System.out.print( "|" );
		Consumer<Long> watcher = ( p ) -> {
			if( p < priorIncrementSteps.get() ) monitorOffset.addAndGet( priorIncrementSteps.get() );
			long stackedProgress = p + monitorOffset.get();

			int currentProgressUnits = (int)(progressUnits * (double)stackedProgress / (double)steps);
			int neededUnits = currentProgressUnits - priorProgressUnits.get();
			if( neededUnits > 0 ) System.out.print( TextUtil.pad( neededUnits, '-' ) );
			if( priorProgressUnits.get() < progressUnits && currentProgressUnits == progressUnits ) System.out.println( "|" );

			priorProgressUnits.set( currentProgressUnits );
			priorIncrementSteps.set( p );
		};
		monitorAll.addListener( watcher );
		monitorOne.addListener( watcher );

		// Start the monitors
		try {
			// Run the test with all threads
			monitorAll.start();
			monitorAll.join();

			// Run the test with one thread
			monitorOne.start();
			monitorOne.join();
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		}

		// End of progress bar
		//System.out.println( "|" );

		double scoreAll = (long)monitorAll.getThroughput( TimeUnit.SECONDS );
		double scoreOne = (long)monitorOne.getThroughput( TimeUnit.SECONDS );
		double sumAll = monitorAll.getStatistics().getSumValue();
		double sumOne = monitorOne.getStatistics().getSumValue();

		double efficiency = scoreAll / scoreOne;
//		if( efficiency < 0.5 ) {
//			// Likely to be running simultaneous multithreading (SMT)
//			efficiency *= 2;
//			scoreAll *= 2;
//			sumAll *= 2;
//		}

		System.out.printf( "Throughput on all threads: %8.0f%n", sumAll );
		System.out.printf( "Score all threads:         %8.0f%n", scoreAll );
		System.out.printf( "Throughput on one thread:  %8.0f%n", sumOne );
		System.out.printf( "Score one thread:          %8.0f%n", scoreOne );
		System.out.printf( "All improvement over one:  %8.1f%n", sumAll / sumOne );
		System.out.printf( "Multi-thread efficiency :  %8.1f%%%n", efficiency * 100 );

		//		AcornChecker checker = new AcornChecker( 16, new HashTest() );
		//		try {
		//			int steps = checker.getStepCount();
		//			System.out.println( "|" + "=".repeat( steps ) + "|" );
		//			System.out.print( "|" );
		//			checker.addListener( ( p ) -> System.out.print( '-' ) );
		//			long score = checker.call();
		//			System.out.println( "|" );
		//			System.out.println( "Acorn score:    " + score );
		//			System.out.println( "Squirrel count: " + AcornChecker.getAvailableCoreCount() );
		//		} catch( Throwable throwable ) {
		//			throwable.printStackTrace( System.err );
		//		}

		System.exit( 0 );
	}

	private void printHeader( ProductCard card ) {
		System.out.println( card.getName() + " " + card.getVersion() );
	}

}
