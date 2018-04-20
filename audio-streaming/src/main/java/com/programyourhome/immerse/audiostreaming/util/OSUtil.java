package com.programyourhome.immerse.audiostreaming.util;

/**
 * Utility methods for Operating System detection.
 */
public class OSUtil {

    public static enum OS {
        LINUX, MAC, WINDOWS
    }

    public static final String OS_NAME = System.getProperty("os.name");

    private OSUtil() {
    }

    public static OS getOS() {
        if (OS_NAME.startsWith("Linux")) {
            return OS.LINUX;
        } else if (OS_NAME.startsWith("Mac")) {
            return OS.MAC;
        } else if (OS_NAME.startsWith("Windows")) {
            return OS.WINDOWS;
        } else {
            throw new IllegalStateException("Unknown OS: " + OS_NAME);
        }
    }

    public static boolean isLinux() {
        return getOS() == OS.LINUX;
    }

    public static boolean isMac() {
        return getOS() == OS.MAC;
    }

    public static boolean isWindows() {
        return getOS() == OS.WINDOWS;
    }

}
