package com.avereon.acorncli.test;

import java.util.Random;

public class LoadTest implements Runnable {

	private final Random r = new Random();

	double value = r.nextDouble();

	@Override
	public void run() {
		value = value/ r.nextDouble();
	}

}
