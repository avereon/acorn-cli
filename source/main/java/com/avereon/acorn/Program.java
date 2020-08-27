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

		AcornChecker checker = new AcornChecker();
		try {
			System.out.println( "|" + "=====".repeat( 3 ) + "|" );
			System.out.print( "|" );
			//checker.addListener( ( c, t ) -> System.out.print( '-' ) );
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
