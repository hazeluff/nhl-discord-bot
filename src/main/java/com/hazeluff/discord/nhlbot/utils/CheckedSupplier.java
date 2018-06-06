package com.hazeluff.discord.nhlbot.utils;

@FunctionalInterface
public interface CheckedSupplier<T> {
	T get() throws Exception;
}
