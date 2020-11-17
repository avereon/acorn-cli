package com.avereon.acorn;

import com.avereon.acorn.test.LoadTest;
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

		int scale = 10;
		AcornChecker checker = new AcornChecker( new LoadTest() );
		try {
			int steps = checker.getStepCount() / scale;
			System.out.println( "|" + "=".repeat( steps ) + "|" );
			System.out.print( "|" );
			checker.addListener( ( p ) -> {
				if( p % scale == 0 ) System.out.print( '-' );
			} );
			long score = checker.call();
			System.out.println( "|" );
			System.out.println( "Squirrel count: " + checker.getCoreCount() );
			System.out.println( "Acorn score:    " + score );
		} catch( Throwable throwable ) {
			throwable.printStackTrace( System.err );
		}
		System.exit( 0 );
	}

	private void printHeader( ProductCard card ) {
		System.out.println( card.getName() + " " + card.getVersion() );
	}

}
