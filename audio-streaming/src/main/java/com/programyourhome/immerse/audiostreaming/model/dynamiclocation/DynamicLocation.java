package com.programyourhome.immerse.audiostreaming.model.dynamiclocation;

import com.programyourhome.immerse.speakermatrix.model.Vector3D;

public interface DynamicLocation {

    public Vector3D getLocation(long millisSinceStart);

}
