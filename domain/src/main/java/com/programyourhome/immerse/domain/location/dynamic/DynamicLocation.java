package com.programyourhome.immerse.domain.location.dynamic;

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

}
