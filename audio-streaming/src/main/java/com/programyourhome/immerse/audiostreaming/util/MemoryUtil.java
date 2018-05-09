package com.programyourhome.immerse.audiostreaming.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

/**
 * Utility methods for JVM memory.
 *
 * NB: The values returned by the method in this class seem very explicit in number of bytes,
 * but reality has proven they are not constantly updated, so might represent somewhat outdated information.
 */
public class MemoryUtil {

    private MemoryUtil() {
    }

    private static MemoryPoolMXBean edenSpace;
    private static MemoryPoolMXBean oldSpace;

    // Statically initialize the memory beans from the ManagementFactory.
    static {
        for (MemoryPoolMXBean mxBean : ManagementFactory.getMemoryPoolMXBeans()) {
            if (mxBean.getName().endsWith("Eden Space")) {
                edenSpace = mxBean;
            } else if (mxBean.getName().endsWith("Old Gen")) {
                oldSpace = mxBean;
            }
        }
    }

    /**
     * Get the amount of free bytes in eden space.
     */
    public static long getFreeEdenSpaceInBytes() {
        return getFreeSpaceInBytes(edenSpace);
    }

    /**
     * Get the amount of free kilobytes in eden space.
     */
    public static double getFreeEdenSpaceInKB() {
        return getFreeSpaceInKB(edenSpace);
    }

    /**
     * Get the amount of free megabytes in eden space.
     */
    public static double getFreeEdenSpaceInMB() {
        return getFreeSpaceInMB(edenSpace);
    }

    /**
     * Get the amount of free bytes in old space.
     */
    public static long getFreeOldSpaceInBytes() {
        return getFreeSpaceInBytes(oldSpace);
    }

    /**
     * Get the amount of free kilobytes in old space.
     */
    public static double getFreeOldSpaceInKB() {
        return getFreeSpaceInKB(oldSpace);
    }

    /**
     * Get the amount of free megabytes in old space.
     */
    public static double getFreeOldSpaceInMB() {
        return getFreeSpaceInMB(oldSpace);
    }

    private static long getFreeSpaceInBytes(MemoryPoolMXBean spaceBean) {
        return spaceBean.getUsage().getCommitted() - spaceBean.getUsage().getUsed();
    }

    private static double getFreeSpaceInKB(MemoryPoolMXBean spaceBean) {
        return getFreeSpaceInBytes(spaceBean) / 1024.0;
    }

    private static double getFreeSpaceInMB(MemoryPoolMXBean spaceBean) {
        return getFreeSpaceInKB(spaceBean) / 1024.0;
    }

}
