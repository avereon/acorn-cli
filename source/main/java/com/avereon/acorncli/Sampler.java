package com.avereon.acorncli;

public class Sampler implements Sample {

	private final Runnable test;

	private final long limit;

	private long count;

	private long time;

	public Sampler( long limitMs, Runnable test ) {
		this.limit = limitMs * 100000;
		this.test = test;
	}

	public long getCount() {
		return count;
	}

	public long getNanos() {
		return time;
	}

	public synchronized long waitFor() throws InterruptedException {
		while( this.time == 0 ) {
			this.wait( 1000 );
		}
		return count;
	}

	@Override
	public Sample call() {
		//System.out.println( "Start test..." );
		Thread thread = Thread.currentThread();
		synchronized( this ) {
			long startTime = System.nanoTime();
			long endTime = startTime + limit;
			while( !thread.isInterrupted() && System.nanoTime() < endTime ) {
				test.run();
				count++;
			}
			this.notifyAll();
			long stopTime = System.nanoTime();
			this.time = stopTime - startTime;
		}
		//System.out.println( "End test." );
		return this;
	}

	@Override
	public String toString() {
		return "count=" + count + " time=" + time;
	}

}
