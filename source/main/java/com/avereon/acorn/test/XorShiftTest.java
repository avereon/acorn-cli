package com.avereon.acorn.test;

import java.util.Random;

public class XorShiftTest implements Runnable {

	private int a = new Random().nextInt();

	@Override
	public void run() {
		int x = a;
		x ^= x << 13;
		x ^= x >> 17;
		x ^= x << 5;
		a = x;
	}

}
