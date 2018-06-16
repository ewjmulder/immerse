package com.programyourhome.immerse.toolbox.location.dynamic;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;

/**
 * A fixed location, static at all times.
 */
public class FixedDynamicLocation implements DynamicLocation {

    private static final long serialVersionUID = Serialization.VERSION;

    private final Vector3D location;

    public FixedDynamicLocation(Vector3D location) {
        this.location = location;
    }

    @Override
    public Vector3D getLocation(long millisSinceStart) {
        return this.location;
    }

    public static Factory<DynamicLocation> fixed(double x, double y, double z) {
        return fixed(new Vector3D(x, y, z));
    }

    public static Factory<DynamicLocation> fixed(Vector3D location) {
        return new Factory<DynamicLocation>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public DynamicLocation create() {
                return new FixedDynamicLocation(location);
            }
        };
    }

}
