package com.hazeluff.discord.canucks.utils;

public class HttpException extends Exception {
	private static final long serialVersionUID = 1336567484487343944L;

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable t) {
		super(t);
	}
}
