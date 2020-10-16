package com.hazeluff.discord.utils;

@FunctionalInterface
public interface CheckedSupplier<T> {
	T get() throws Exception;
}
