package com.avereon.acorncli;

public class Statistics {

	private final long[] values;

	private final long[] times;

	private int index;

	private long minValue;

	private long maxValue;

	private long avgValue;

	private long sumValue;

	private long minTime;

	private long maxTime;

	private long avgTime;

	private long sumTime;

	private double jitValue;

	public Statistics( int size ) {
		values = new long[ size ];
		times = new long[ size ];
	}

	public void add( long value, long timeNs ) {
		values[ index ] = value;
		times[ index ] = timeNs;
		index++;
	}

	public Statistics process() {
		processValues( values, times );
		return this;
	}

	public long getMinValue() {
		return minValue;
	}

	public long getMaxValue() {
		return maxValue;
	}

	public long getAvgValue() {
		return avgValue;
	}

	public long getSumValue() {
		return sumValue;
	}

	public long getMinTime() {
		return minTime;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public long getAvgTime() {
		return avgTime;
	}

	public long getSumTime() {
		return sumTime;
	}

	public double getJitter() {
		return jitValue;
	}

	public String toString() {
		return "minValue=" + minValue + " maxValue=" + maxValue + " avgValue=" + avgValue + " jitValue=" + jitValue;
	}

	private void processValues( long[] values, long[] times ) {
		int count = values.length;

		long value;
		long time;
		long localMinValue = Long.MAX_VALUE;
		long localMaxValue = 0;
		long localSumValue = 0;
		long localMinTime = Long.MAX_VALUE;
		long localMaxTime = 0;
		long localSumTime = 0;
		for( int index = 0; index < count; index++ ) {
			value = values[ index ];
			time = times[ index ];

			localSumValue += value;
			localSumTime += time;
			localMinValue = Math.min( localMinValue, value );
			localMaxValue = Math.max( localMaxValue, value );
			localMinTime = Math.min( localMinTime, time );
			localMaxTime = Math.max( localMaxTime, time );
		}

		minValue = localMinValue;
		avgValue = localSumValue / count;
		maxValue = localMaxValue;
		sumValue = localSumValue;
		minTime = localMinTime;
		avgTime = localSumTime / count;
		maxTime = localMaxTime;
		sumTime = localSumTime;

		jitValue = (minValue - maxValue) / (double)avgValue;
	}

}
