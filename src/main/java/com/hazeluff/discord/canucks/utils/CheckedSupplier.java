package com.hazeluff.discord.canucks.utils;

@FunctionalInterface
public interface CheckedSupplier<T> {
	T get() throws Exception;
}
