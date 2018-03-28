package com.programyourhome.immerse.domain.location.dynamic;

import java.util.SortedMap;

import com.programyourhome.immerse.domain.location.Vector3D;

public interface DynamicLocation {

    public Vector3D getLocation(long millisSinceStart);

    public static DynamicLocation fixed(double x, double y, double z) {
        return fixed(new Vector3D(x, y, z));
    }

    public static DynamicLocation fixed(Vector3D location) {
        return new FixedDynamicLocation(location);
    }

    public static DynamicLocation keyFrames(SortedMap<Long, Vector3D> keyFrames) {
        return new KeyFrameDynamicLocation(keyFrames);
    }

}
