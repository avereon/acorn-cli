package com.avereon.acorncli;

import com.avereon.acorncli.test.LoadTest;
import com.avereon.product.ProductCard;
import lombok.CustomLog;

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

		AcornChecker checker = new AcornChecker( new LoadTest(), new LoadTest() );
		try {
			int steps = checker.getStepCount();
			System.out.println( "|" + "=".repeat( steps ) + "|" );
			System.out.print( "|" );
			checker.addListener( ( p ) -> System.out.print( '-' ) );
			long score = checker.call();
			System.out.println( "|" );
			System.out.println( "Acorn score:    " + score );
			System.out.println( "Squirrel count: " + checker.getAvailableCoreCount() );
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		}
		System.exit( 0 );
	}

	private void printHeader( ProductCard card ) {
		System.out.println( card.getName() + " " + card.getVersion() );
	}

}
