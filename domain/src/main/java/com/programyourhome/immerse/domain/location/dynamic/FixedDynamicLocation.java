package com.programyourhome.immerse.domain.location.dynamic;

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

}
