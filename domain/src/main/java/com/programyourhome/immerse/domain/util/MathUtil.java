package com.programyourhome.immerse.domain.util;

import org.la4j.Vector;

import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.Speaker;

public class MathUtil {

    private MathUtil() {
    }

    public static double calculateAngleInDegrees(Snapshot snapshot, Speaker speakerPojo) {
        Vector listener = snapshot.getListener().toLa4j();
        Vector source = snapshot.getSource().toLa4j();
        Vector speaker = speakerPojo.getVectorLa4j();
        double angle = calculateAngleInDegrees(listener, source, speaker);
        // In case of the listener being at exactly the same position as either the source or the speaker,
        // there is no angle (NaN), so fall back to a default value.
        if (angle == Double.NaN) {
            if (listener.equals(source)) {
                // If the listener is 'in' the source, all speakers are equal.
                angle = 0;
            } else {
                // If the listener is 'in' the speaker, 'disable' that speaker by giving it the max angle.
                angle = 180;
            }
        }
        return angle;
    }

    public static double calculateAngleInDegrees(Vector base, Vector target1, Vector target2) {
        Vector baseToTarget1 = target1.subtract(base);
        Vector baseToTarget1Direction = normalize(baseToTarget1);
        Vector baseToTarget2 = target2.subtract(base);
        Vector baseToTarget2Direction = normalize(baseToTarget2);
        double dotProduct = safeDotProduct(baseToTarget1Direction, baseToTarget2Direction);
        double angleInRadians = Math.acos(dotProduct);
        return Math.toDegrees(angleInRadians);
    }

    /**
     * Calculate the dot product.
     * Correct for double imprecision if the result is out of bounds, so acos will always work on the result.
     */
    public static double safeDotProduct(Vector v1, Vector v2) {
        double dotProduct = v1.innerProduct(v2);
        if (dotProduct > 1) {
            dotProduct = 1;
        }
        if (dotProduct < -1) {
            dotProduct = -1;
        }
        return dotProduct;
    }

    public static double calculateFraction(double min, double max, double value) {
        if (value < min || value > max) {
            throw new IllegalArgumentException("Value " + value + " must be between " + min + " and " + max);
        }
        double diff = max - min;
        double fraction;
        if (diff == 0) {
            // Special case: no diff, so default to 1.
            fraction = 1;
        } else {
            fraction = (value - min) / diff;
        }
        return fraction;
    }

    public static double calculateValueInRange(final double minValue, final double maxValue, final double fraction) {
        return minValue + fraction * (maxValue - minValue);
    }

    public static Vector normalize(Vector input) {
        return input.divide(input.euclideanNorm());
    }

    public static double reverseAngle(double angle) {
        return 180 - angle;
    }

}
