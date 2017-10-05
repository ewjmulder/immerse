package com.programyourhome.immerse.audiostreaming.model.dynamiclocation;

import com.programyourhome.immerse.speakermatrix.model.Vector3D;

public class HorizontalCircleDynamicLocation implements DynamicLocation {

    private double centerX;
    private double centerY;
    private double z;
    private double startAngleInDegrees;
    private double radius;
    private boolean clockwise;
    private double millisPerDegreeAngle;
    // TODO: support for secondsPerFullCircle

    public HorizontalCircleDynamicLocation(double centerX, double centerY, double z, double startAngleInDegrees, double radius, boolean clockwise,
            double millisPerDegreeAngle) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.z = z;
        // Minus 90 degrees to start at the 'top' of the circle (since the unit circle starts at the 'right').
        this.startAngleInDegrees = startAngleInDegrees - 90;
        this.radius = radius;
        this.clockwise = clockwise;
        this.millisPerDegreeAngle = millisPerDegreeAngle;
    }

    @Override
    public Vector3D getLocation(long millisSinceStart) {
        double angleMoved = (millisSinceStart / this.millisPerDegreeAngle);
        if (!this.clockwise) {
            angleMoved *= -1;
        }
        double currentAngleInDegrees = this.startAngleInDegrees + angleMoved;
        double x = this.centerX + Math.cos(Math.toRadians(currentAngleInDegrees)) * this.radius;
        double y = this.centerY + Math.sin(Math.toRadians(currentAngleInDegrees)) * this.radius;
        // Mirror y to let and increase in angle mean clockwise rotation.
        y *= -1;
        return new Vector3D(x, y, this.z);
    }

    // TODO: This screams for a good unit test!!!
    public static void main(String[] args) {
        HorizontalCircleDynamicLocation h = new HorizontalCircleDynamicLocation(0, 0, 0, 90, 1, true, 1);
        System.out.println(h.getLocation(0));
    }

}
