package com.hazeluff.discord.canucks.utils;

import java.util.Optional;

public class EugeneUtils {
	public static void main(String[] argv) {
		System.out.println(Optional.ofNullable("asdf").map(str -> {
			throw new RuntimeException();
		}).orElse(null));
	}
}