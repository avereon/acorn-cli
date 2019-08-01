package com.avereon.acorn;

import java.util.Random;

public class XorShiftCounter extends Counter {

	private int a = new Random().nextInt();

	@Override
	public void task() {
		int x = a;
		x ^= x << 13;
		x ^= x >> 17;
		x ^= x << 5;
		a = x;
	}

}
