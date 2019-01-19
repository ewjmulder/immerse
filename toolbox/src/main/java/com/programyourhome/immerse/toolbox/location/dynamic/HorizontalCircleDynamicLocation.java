package com.programyourhome.immerse.toolbox.location.dynamic;

import com.programyourhome.immerse.domain.AbstractDynamicData;
import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;

/**
 * Dynamic location that circles 'horizontally' around a certain center with a certain radius
 * at a certain speed, either clockwise or not.
 */
public class HorizontalCircleDynamicLocation extends AbstractDynamicData<Vector3D> implements DynamicLocation {

    private static final long serialVersionUID = Serialization.VERSION;

    private final double centerX;
    private final double centerY;
    private final double z;
    private final double startAngleInDegrees;
    private final double radius;
    private final boolean clockwise;
    private final double millisPerDegreeAngle;

    public HorizontalCircleDynamicLocation(Vector3D center, double startAngleInDegrees, double radius, boolean clockwise, double millisPerFullCircle) {
        super(true);
        this.centerX = center.getX();
        this.centerY = center.getY();
        this.z = center.getZ();
        // Minus 90 degrees to start at the 'top' of the circle (since the unit circle starts at the 'right').
        this.startAngleInDegrees = startAngleInDegrees - 90;
        this.radius = radius;
        this.clockwise = clockwise;
        this.millisPerDegreeAngle = millisPerFullCircle / 360;
    }

    @Override
    public Vector3D getCurrentValue() {
        double angleMoved = this.getMillisSinceStart() / this.millisPerDegreeAngle;
        if (!this.clockwise) {
            angleMoved *= -1;
        }
        double currentAngleInDegrees = this.startAngleInDegrees + angleMoved;
        double x = Math.cos(Math.toRadians(currentAngleInDegrees)) * this.radius;
        double y = Math.sin(Math.toRadians(currentAngleInDegrees)) * this.radius;
        // Mirror y to let an increase in angle mean clockwise rotation.
        y *= -1;

        return new Vector3D(this.centerX + x, this.centerY + y, this.z);
    }

    public static Factory<DynamicLocation> horizontalCircle(Vector3D center, double startAngleInDegrees,
            double radius, boolean clockwise, double millisPerFullCircle) {
        return new Factory<DynamicLocation>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public DynamicLocation create() {
                return new HorizontalCircleDynamicLocation(center, startAngleInDegrees, radius, clockwise, millisPerFullCircle);
            }
        };
    }

}
