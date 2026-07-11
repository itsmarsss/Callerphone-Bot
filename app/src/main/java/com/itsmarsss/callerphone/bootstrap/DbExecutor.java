package com.itsmarsss.callerphone.bootstrap;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Bounded executor for sync Mongo work off JDA gateway threads. */
public final class DbExecutor {
    private final ExecutorService executor;

    public DbExecutor(int threads) {
        this.executor = Executors.newFixedThreadPool(Math.max(2, threads), r -> {
            Thread t = new Thread(r, "match-db");
            t.setDaemon(true);
            return t;
        });
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
