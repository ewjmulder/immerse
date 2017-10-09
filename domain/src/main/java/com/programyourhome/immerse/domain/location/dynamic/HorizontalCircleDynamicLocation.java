package com.programyourhome.immerse.domain.location.dynamic;

import com.programyourhome.immerse.domain.location.Vector3D;

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
        double x = Math.cos(Math.toRadians(currentAngleInDegrees)) * this.radius;
        double y = Math.sin(Math.toRadians(currentAngleInDegrees)) * this.radius;
        // Mirror y to let and increase in angle mean clockwise rotation.
        y *= -1;

        // System.out.println("millisSinceStart: " + millisSinceStart);
        // System.out.println("currentAngleInDegrees: " + currentAngleInDegrees);
        // System.out.println("radius: " + this.radius);
        // System.out.println("x: " + x);
        // System.out.println("y: " + y);

        return new Vector3D(this.centerX + x, this.centerY + y, this.z);
    }

    // TODO: This screams for a good unit test!!!
    public static void main(String[] args) {
        HorizontalCircleDynamicLocation h = new HorizontalCircleDynamicLocation(0, 0, 0, 90, 1, true, 1);
        System.out.println(h.getLocation(0));
    }

}
