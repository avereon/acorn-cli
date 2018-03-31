package com.xeomar.acorn;

import java.util.Random;

public class RandomCounter extends Counter {

	private Random r = new Random();

	@Override
	public void task() {
		r.nextLong();
	}

}
