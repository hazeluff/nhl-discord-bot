package com.hazeluff.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ThrowableAssertTest {
	@Test
	public void assertExceptionShouldReturnThrowableAssertWhenExceptionIsThrown() {
		ThrowableAssert throwableAssert = ThrowableAssert.assertException(() -> { throw new RuntimeException(); });

		assertNotNull(throwableAssert);
	}

	@Test(expected = AssertionError.class)
	public void assertExceptionShouldThrowAssertionErrorWhenNoExceptionIsThrown() {
		ThrowableAssert.assertException(()-> {});
	}
	
	@Test
	public void isInstanceOfShouldPassWhenClassIsTheSame() {
		ThrowableAssert.assertException(() -> { throw new RuntimeException(); })
				.isInstanceOf(RuntimeException.class);
	}
	
	@Test(expected = AssertionError.class)
	public void isInstanceOfShouldFailWhenClassIsNotTheSame() {
		ThrowableAssert.assertException(() -> { throw new RuntimeException(); })
				.isInstanceOf(IllegalArgumentException.class);
	}
}
