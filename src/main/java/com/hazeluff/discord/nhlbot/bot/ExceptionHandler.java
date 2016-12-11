package com.hazeluff.discord.nhlbot.bot;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler implements UncaughtExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.error("UnhandledException in [" + t.getName() + "]", e);
	}

}
