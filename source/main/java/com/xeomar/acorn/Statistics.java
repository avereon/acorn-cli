package com.xeomar.acorn;

public class Statistics {

	private long min;

	private long max;

	private long avg;

	private double jitter;

	public Statistics( long[] values, long time ) {
		processValues( values, time );
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public long getAvg() {
		return avg;
	}

	public double getJitter() {
		return jitter;
	}

	public String toString() {
		return "min=" + min + " max=" + max + " avg=" + avg + " jit=" + jitter;
	}

	private void processValues( long[] values, long time ) {
		int count = values.length;

		long value;
		long average = 0;
		long coefficient = 0;
		long minimum = Long.MAX_VALUE;
		long maximum = Long.MIN_VALUE;
		for( int index = 0; index < count; index++ ) {
			value = values[ index ];
			minimum = Math.min( minimum, value );
			maximum = Math.max( maximum, value );
			average = (coefficient * average + value) / (coefficient + 1);
			coefficient++;
		}

		min = minimum / time;
		max = maximum / time;
		avg = average / time;

		jitter = (maximum - minimum) / (double)average;
	}

}
