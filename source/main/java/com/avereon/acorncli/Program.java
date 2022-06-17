package com.avereon.acorncli;

import com.avereon.acorncli.test.HashTest;
import com.avereon.product.ProductCard;
import com.avereon.util.TextUtil;
import lombok.CustomLog;

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

		double scoreAll = monitorAll.getScore();
		double scoreOne = monitorOne.getScore();
		double sumAll = monitorAll.getStatistics().getSumValue();
		double sumOne = monitorOne.getStatistics().getSumValue();
		double efficiency = (scoreAll / monitorAll.getRequestedThreads()) / scoreOne;

		System.out.printf( "%-30s %8d%n", "Thread count:", monitorAll.getRequestedThreads() );
		System.out.printf( "%-30s %8.0f%n", "Score all threads:", scoreAll );
		System.out.printf( "%-30s %8.0f%n", "Score one thread:", scoreOne );
		System.out.printf( "%-30s %8.1f%n", "All improvement over one:", sumAll / sumOne );
		System.out.printf( "%-30s %8.1f%%%n", "Multi-thread efficiency:", efficiency * 100 );

		System.exit( 0 );
	}

	private void printHeader( ProductCard card ) {
		System.out.println( card.getName() + " " + card.getVersion() );
	}

}
