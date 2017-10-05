package com.programyourhome.immerse.domain.location.dynamic;

import com.programyourhome.immerse.domain.location.Vector3D;

public class StaticDynamicLocation implements DynamicLocation {

    private Vector3D staticLocation;

    public StaticDynamicLocation(Vector3D staticLocation) {
        this.staticLocation = staticLocation;
    }

    @Override
    public Vector3D getLocation(long millisSinceStart) {
        return this.staticLocation;
    }

}
