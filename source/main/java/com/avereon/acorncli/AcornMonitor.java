package com.avereon.acorncli;

import com.avereon.acorncli.test.HashTest;
import lombok.CustomLog;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@CustomLog
public class AcornMonitor implements AcornCounter {

	// Sample times less that 1000ms tend to be erratic
	private static final long SAMPLE_TIME_MS = 5000;

	private static final long TIME_PER_STEP = 100;

	private static final Timer timer = new Timer( "Monitor timer", true );

	private final int requestedThreads;

	private final Runnable test;

	private final ExecutorService executor;

	private final Collection<Sampler> samplers;

	private Set<Future<Sampler>> futures;

	private final Set<Consumer<Long>> listeners;

	private final long total;

	private long steps;

	private FireEventTask eventTrigger;

	public AcornMonitor() {
		this( 1 );
	}

	public AcornMonitor(int requestedThreads ) {
		this( requestedThreads, new HashTest() );
	}

	public AcornMonitor( int requestedThreads, Runnable test ) {
		this.total = SAMPLE_TIME_MS / TIME_PER_STEP;
		this.requestedThreads = requestedThreads;
		this.test = test;

		this.executor = Executors.newFixedThreadPool( requestedThreads, new DaemonThreadFactory() );
		this.listeners = new CopyOnWriteArraySet<>();

		samplers = new HashSet<>();
		for( int testerIndex = 0; testerIndex < requestedThreads; testerIndex++ ) {
			samplers.add( new Sampler( SAMPLE_TIME_MS, test ) );
		}
	}

	public Runnable getTest() {
		return test;
	}

	public int getRequestedThreads() {
		return requestedThreads;
	}

	@Override
	public long getTotal() {
		return total;
	}

	@Override
	public long getScore() {
		return (long)getThroughput( TimeUnit.SECONDS );
	}

	@Override
	public void addListener( Consumer<Long> runnable ) {
		listeners.add( runnable );
	}

	@Override
	public void removeListener( Consumer<Long> runnable ) {
		listeners.remove( runnable );
	}

	@Override
	public AcornMonitor start() {
		futures = samplers.stream().map( executor::submit ).collect( Collectors.toSet() );
		timer.schedule( eventTrigger = new FireEventTask(), TIME_PER_STEP, TIME_PER_STEP );
		return this;
	}

	@Override
	public boolean isRunning() {
		return !futures.isEmpty() && !executor.isTerminated();
	}

	@Override
	public AcornMonitor join() {
		try {
			for( Future<Sampler> future : futures ) {
				future.get();
			}
		} catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
		eventTrigger.cancel();
		fireEvent( total );
		return this;
	}

	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	@Override
	public AcornCounter stop() {
		executor.shutdownNow();
		try {
			executor.awaitTermination( 1, TimeUnit.SECONDS );
		} catch( InterruptedException exception ) {
			throw new RuntimeException( exception );
		}
		return this;
	}

	public Statistics getStatistics() {
		Statistics stats = new Statistics( futures.size() );

		futures.forEach( f -> {
			try {
				Sampler sampler = f.get();
				stats.add( sampler.getCount(), sampler.getNanos() );
			} catch( Exception e ) {
				throw new RuntimeException( e );
			}
		} );

		return stats.process();
	}

	public double getThroughput( TimeUnit unit ) {
		Statistics stats = getStatistics();
		double throughputNanos = (double)stats.getAvgValue() / (double)stats.getAvgTime();
		log.atConfig().log("tp="+throughputNanos + " * " + TimeUnit.NANOSECONDS.convert( 1, unit ));
		return throughputNanos * TimeUnit.NANOSECONDS.convert( 1, unit );
	}

	private void fireEvent( long step ) {
		for( Consumer<Long> listener : new HashSet<>( listeners ) ) {
			listener.accept( step );
		}
	}

	private static final class DaemonThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread( Runnable r ) {
			Thread thread = new Thread( r );
			thread.setDaemon( true );
			return thread;
		}

	}

	private final class FireEventTask extends TimerTask {

		@Override
		public void run() {
			fireEvent( ++steps );
		}

	}

}
