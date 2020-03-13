package com.hazeluff.test;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;

public class ThrowableAssert {

	private Throwable caughtException;

	private ThrowableAssert(Throwable caughtException) {
		this.caughtException = caughtException;
	}

	public static ThrowableAssert assertException(Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable caught) {
			return new ThrowableAssert(caught);
		}
		throw new AssertionError("No exception was thrown");
	}

	@SuppressWarnings("unchecked")
	public ThrowableAssert isInstanceOf(Class<? extends Throwable> exceptionClass) {
		MatcherAssert.assertThat(caughtException, Is.isA((Class<Throwable>) exceptionClass));
		return this;
	}
}
