package com.programyourhome.immerse.audiostreaming.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncUtil {

    private AsyncUtil() {
    }

    private static ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Submit a task to be executed asynchronously on the executor service.
     * Also log an exception if thrown during task execution.
     */
    public static void submitAsyncTask(Runnable task) {
        executorService.submit(() -> LogUtil.logExceptions(task));
    }

}
