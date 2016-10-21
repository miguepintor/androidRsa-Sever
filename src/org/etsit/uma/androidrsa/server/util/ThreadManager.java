package org.etsit.uma.androidrsa.server.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadManager {
	private static final ThreadFactory factory = new ExceptionThreadFactory(new ExceptionHandler());
	private static final ExecutorService executor = Executors.newSingleThreadExecutor(factory);

	public static void execute(Runnable task) {
		executor.execute(task);
	}

	private static class ExceptionThreadFactory implements ThreadFactory {
		private static final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
		private final Thread.UncaughtExceptionHandler handler;

		public ExceptionThreadFactory(Thread.UncaughtExceptionHandler handler) {
			this.handler = handler;
		}

		@Override
		public Thread newThread(Runnable run) {
			Thread thread = defaultFactory.newThread(run);
			thread.setUncaughtExceptionHandler(handler);
			return thread;
		}
	}

	private static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread thread, Throwable t) {
			t.printStackTrace();
		}
	}
}
