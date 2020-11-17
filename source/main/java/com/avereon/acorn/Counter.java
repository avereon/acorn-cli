package com.avereon.acorn;

public class Counter {

	private Runnable runnable;

	private Thread thread;

	private long count;

	private long time;

	protected Counter() {}

	public Counter( Runnable runnable ) {
		this.runnable = runnable;
	}

	public Counter start() {
		thread = new Thread( this::run );
		thread.setDaemon( true );
		thread.start();
		return this;
	}

	public Counter stop() {
		thread.interrupt();
		return this;
	}

	public long runFor( long time ) throws InterruptedException {
		count = 0;
		start();
		Thread.sleep( time );
		stop();
		return count;
	}

	public synchronized long getCount() {
		return count;
	}

	public synchronized long getTime() {
		return time;
	}

	private synchronized void run() {
		long startTime = System.nanoTime();
		while( !thread.isInterrupted() ) {
			runnable.run();
			count++;
		}
		long stopTime = System.nanoTime();
		time = stopTime - startTime;
	}

}
