package com.xeomar.acorn;

import java.util.Random;

public class AddCounter extends Counter {

	private Random r = new Random();

	private int s;

	@Override
	public void task() {
		s = r.nextInt() * r.nextInt();
	}

}
