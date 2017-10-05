package com.programyourhome.immerse.audiostreaming.model.dynamiclocation.mathfunction;

public class CircleMathFunction implements MathFunction {

    private double startValue;
    private double startAngleInDegrees;
    private double radius;
    private double millisPerDegreeAngle;

    public CircleMathFunction(double startValue, double startAngleInDegrees, double radius, double millisPerDegreeAngle) {

        // TODO: refactor this! to be math sanity

        // this.startValue = startValue;
        // this.startAngleInDegrees = startAngleInDegrees;
        // this.radius = radius;
        // this.millisPerDegreeAngle = millisPerDegreeAngle;
        // // TODO: validate between 0 and 359 or use modulus
        // startAngleInDegrees
    }

    @Override
    public double getValue(long millisSinceStart) {
        return 0;
    }

}
