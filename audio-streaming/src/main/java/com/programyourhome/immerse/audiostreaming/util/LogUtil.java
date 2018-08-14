package com.programyourhome.immerse.audiostreaming.util;

import org.pmw.tinylog.Logger;

public class LogUtil {

    private LogUtil() {
    }

    public static void logExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            Logger.error(e, "Exception during asynchronous task");
        }
    }

}
