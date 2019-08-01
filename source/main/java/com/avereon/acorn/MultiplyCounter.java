package com.avereon.acorn;

import java.util.Random;

public class MultiplyCounter extends Counter {

	private Random r = new Random();

	private int s;

	@Override
	public void task() {
		s = r.nextInt() * r.nextInt();
	}

}
