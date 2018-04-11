package com.programyourhome.immerse.domain.location.dynamic;

import java.util.SortedMap;

import com.programyourhome.immerse.domain.location.Vector3D;

/**
 * A location that changes over time.
 */
public interface DynamicLocation {

    /**
     * Get the location on a certain moment in time.
     * For robustness, implementations are advised to accept any value, both negative and positive.
     */
    public Vector3D getLocation(long millisSinceStart);

    /**
     * A fixed location, static at all times.
     */
    public static DynamicLocation fixed(double x, double y, double z) {
        return fixed(new Vector3D(x, y, z));
    }

    /**
     * A fixed location, static at all times.
     */
    public static DynamicLocation fixed(Vector3D location) {
        return new FixedDynamicLocation(location);
    }

    /**
     * A location based on a path, defined by key frames.
     */
    public static DynamicLocation keyFrames(SortedMap<Long, Vector3D> keyFrames) {
        return new KeyFrameDynamicLocation(keyFrames);
    }

}
