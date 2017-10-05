package com.programyourhome.immerse.domain.location.dynamic;

import com.programyourhome.immerse.domain.location.Vector3D;

public interface DynamicLocation {

    public Vector3D getLocation(long millisSinceStart);

}
