package com.avereon.acorn.test;

import java.util.Random;

public class MultiplyTest implements Runnable {

	private final Random r = new Random();

	private int s;

	@Override
	public void run() {
		s = r.nextInt() * r.nextInt();
	}

}
