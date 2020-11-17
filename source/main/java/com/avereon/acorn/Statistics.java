package com.avereon.acorn;

public class Statistics {

	private long[] values;

	private long[] times;

	private int index;

	private long min;

	private long max;

	private long avg;

	private double jitter;

	public Statistics( int size ) {
		values = new long[ size ];
		times = new long[ size ];
	}

	public void add( long value, long time ) {
		values[ index ] = value;
		times[ index ] = time;
		index++;
	}

	public void process() {
		processValues( values, times );
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

	private void processValues( long[] values, long[] times ) {
		int count = values.length;

		long value;
		long time;
		long average = 0;
		long coefficient = 0;
		long minimum = Long.MAX_VALUE;
		long maximum = 0;
		for( int index = 0; index < count; index++ ) {
			value = values[ index ];
			time = times[ index ];

			double valuesPerNano = (double)value / (double)time;
			double nanosPerValue = (double)time / (double)value;
			double valuesPerMilli = valuesPerNano * 1000000;

			//System.err.println( "index=" + index + "  value=" + value + "  time=" + time );
			//System.err.println( "  vpm=" + valuesPerMilli );
			long timeCorrectedValue = (long)valuesPerMilli;

			minimum = Math.min( minimum, timeCorrectedValue );
			average = (coefficient * average + timeCorrectedValue) / (coefficient + 1);
			maximum = Math.max( maximum, timeCorrectedValue );

			coefficient++;
		}

		min = minimum;
		avg = average;
		max = maximum;

		jitter = (maximum - minimum) / (double)average;
		//System.err.println( "min=" + minimum + "  avg=" + average + "  max=" + maximum + "  jit=" + jitter );
	}

}
