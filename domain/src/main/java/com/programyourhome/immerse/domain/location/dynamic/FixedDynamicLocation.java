package com.programyourhome.immerse.domain.location.dynamic;

import com.programyourhome.immerse.domain.location.Vector3D;

public class FixedDynamicLocation implements DynamicLocation {

    private Vector3D location;

    public FixedDynamicLocation(Vector3D location) {
        this.location = location;
    }

    @Override
    public Vector3D getLocation(long millisSinceStart) {
        return this.location;
    }

}
