package com.programyourhome.immerse.domain.location.dynamic;

import java.util.function.Supplier;

import com.programyourhome.immerse.domain.location.Vector3D;

/**
 * A fixed location, static at all times.
 */
public class FixedDynamicLocation implements DynamicLocation {

    private final Vector3D location;

    public FixedDynamicLocation(Vector3D location) {
        this.location = location;
    }

    @Override
    public Vector3D getLocation(long millisSinceStart) {
        return this.location;
    }

    public static Supplier<DynamicLocation> fixed(double x, double y, double z) {
        return fixed(new Vector3D(x, y, z));
    }

    public static Supplier<DynamicLocation> fixed(Vector3D location) {
        return () -> new FixedDynamicLocation(location);
    }

}
