package com.hazeluff.discord.canucks.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DiscordThreadFactory {

	private static DiscordThreadFactory factory;

	public final List<DiscordThread<?>> threads;

	private DiscordThreadFactory() {
		threads = new CopyOnWriteArrayList<>();
	}

	public static DiscordThreadFactory getInstance() {
		synchronized (DiscordThreadFactory.class) {
			if (factory == null) {
				factory = new DiscordThreadFactory();
			}
		}
		return factory;
	}

	public <T> DiscordThread<T> createThread(Class<T> source, Runnable runnable) {
		DiscordThread<T> thread = DiscordThread.create(source, runnable);
		threads.add(thread);
		return thread;
	}


	public static class DiscordThread<T> extends Thread {
		private final Class<T> source;

		private DiscordThread(Class<T> source, Runnable runnable) {
			super(runnable);
			this.source = source;
		}

		public static <U> DiscordThread<U> create(Class<U> source, Runnable runnable) {
			return new DiscordThread<U>(source, runnable);
		}

		@Override
		public void run() {
			super.run();
			DiscordThreadFactory.getInstance().threads.remove(this);
		}

		public Class<T> getSource() {
			return source;
		}
	}

	public long getNumThreads(Class<?> source) {
		return threads.stream().filter(thread -> thread.getSource().equals(source)).count();
	}
}
