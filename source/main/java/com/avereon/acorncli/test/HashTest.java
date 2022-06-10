package com.avereon.acorncli.test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.function.Supplier;

public class HashTest implements Runnable {

	private static final Random random = new Random();

	private static final byte[] data = generateRandomData( (int)Math.pow( 2, 20 ) );

	private final ThreadLocal<MessageDigest> digest;

	public HashTest() {
		this.digest = ThreadLocal.withInitial( new MessageDigestSupplier() );
	}

	private static class MessageDigestSupplier implements Supplier<MessageDigest> {

		@Override
		public MessageDigest get() {
			try {
				return MessageDigest.getInstance( "SHA3-256" );
			} catch( NoSuchAlgorithmException exception ) {
				throw new RuntimeException( exception );
			}
		}

	}

	@Override
	public void run() {
		digest.get().digest( data );
	}

	private static byte[] generateRandomData( int size ) {
		byte[] data = new byte[ size ];
		random.nextBytes( data );
		return data;
	}

}
