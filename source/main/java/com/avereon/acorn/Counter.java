package com.avereon.acorn;

public abstract class Counter {

	private Thread thread;

	private long startTime;

	private long stopTime;

	private long count;

	public abstract void task();

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

	public long getCount() {
		return count;
	}

	private void run() {
		this.startTime = System.nanoTime();
		while( !thread.isInterrupted() ) {
			task();
			count++;
		}
		this.stopTime = System.nanoTime();
	}

}
