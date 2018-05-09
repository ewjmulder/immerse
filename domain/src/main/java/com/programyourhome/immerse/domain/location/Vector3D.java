package com.programyourhome.immerse.domain.location;

import java.io.Serializable;

import org.la4j.Vector;

import com.programyourhome.immerse.domain.Serialization;

/**
 * A vector in 3D space.
 */
public class Vector3D implements Serializable {

    private static final long serialVersionUID = Serialization.VERSION;

    private final double x;
    private final double y;
    private final double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public Vector toLa4j() {
        return Vector.fromArray(new double[] { this.x, this.y, this.z });
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vector3D
                && ((Vector3D) obj).x == this.x
                && ((Vector3D) obj).y == this.y
                && ((Vector3D) obj).z == this.z;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public static Vector3D fromLa4j(Vector vector) {
        if (vector.length() != 3) {
            throw new IllegalArgumentException("Provided vector must be of length 3");
        }
        return new Vector3D(vector.get(0), vector.get(1), vector.get(2));
    }

}
