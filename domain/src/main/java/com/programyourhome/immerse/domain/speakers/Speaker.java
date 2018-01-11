package com.programyourhome.immerse.domain.speakers;

import org.la4j.Vector;

import com.programyourhome.immerse.domain.location.Vector3D;

public class Speaker {

    private int id;
    private String name;
    private String description;
    private double multiplier;
    private Vector3D vector3D;

    public Speaker(int id, String name, String description, double multiplier, Vector3D vector3D) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.multiplier = multiplier;
        this.vector3D = vector3D;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    //TODO: integrate multiplier!
    public double getMultiplier() {
        return this.multiplier;
    }

    public Vector3D getVector3D() {
        return this.vector3D;
    }

    public Vector getVectorLa4j() {
        return this.vector3D.toLa4j();
    }

    @Override
    public String toString() {
        return this.getId() + "-" + this.getName() + " @ " + this.vector3D;
    }

}
