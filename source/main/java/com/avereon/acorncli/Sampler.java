package com.avereon.acorncli;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class Sampler implements Callable<Sampler> {

	private static final long CONVERSION = TimeUnit.NANOSECONDS.convert( 1, TimeUnit.MILLISECONDS );

	private final Runnable test;

	private final long duration;

	private long count;

	private long time;

	public Sampler( long limitMs, Runnable test ) {
		this.duration = limitMs * CONVERSION;
		this.test = test;
	}

	public synchronized void waitFor() throws InterruptedException {
		while( this.time == 0 ) {
			this.wait( duration / CONVERSION, (int)(duration % CONVERSION) );
		}
	}

	public long getCount() {
		return count;
	}

	public long getNanos() {
		return time;
	}

	@Override
	public Sampler call() {
		//System.out.println( "Start test..." );
		Thread thread = Thread.currentThread();
		synchronized( this ) {
			long startTime = System.nanoTime();
			long endTime = startTime + duration;
			while( !thread.isInterrupted() && System.nanoTime() < endTime ) {
				test.run();
				count++;
			}
			this.notifyAll();
			long stopTime = System.nanoTime();
			this.time = stopTime - startTime;
		}
		//System.out.println( "End test." );
		//System.out.println( this );
		return this;
	}

	@Override
	public String toString() {
		return "count=" + count + " time=" + time;
	}

}
