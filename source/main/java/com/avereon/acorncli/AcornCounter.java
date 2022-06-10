package com.avereon.acorncli;

import com.avereon.skill.Controllable;
import com.avereon.skill.Joinable;

import java.util.function.Consumer;

public interface AcornCounter extends Controllable<AcornCounter>, Joinable<AcornCounter> {

	long getTotal();

	long getScore();

	void addListener( Consumer<Long> runnable );

	void removeListener( Consumer<Long> runnable );

}
