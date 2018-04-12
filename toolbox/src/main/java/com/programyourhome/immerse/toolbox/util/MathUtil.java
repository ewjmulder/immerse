package com.programyourhome.immerse.toolbox.util;

import org.la4j.Vector;

import com.programyourhome.immerse.domain.Snapshot;
import com.programyourhome.immerse.domain.speakers.Speaker;

/**
 * Math utility functions.
 */
public class MathUtil {

    private MathUtil() {
    }

    /**
     * Calculate the angle (in degrees) between the line listener->source and listener->speaker.
     */
    public static double calculateAngleInDegrees(Snapshot snapshot, Speaker speakerPojo) {
        Vector listener = snapshot.getListener().toLa4j();
        Vector source = snapshot.getSource().toLa4j();
        Vector speaker = speakerPojo.getPosition().toLa4j();
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

    /**
     * Calculate the angle (in degrees) between the vectors base->target1 and base->target2.
     */
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
     * Calculate the dot product of 2 vectors.
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

    /**
     * Calculate the fraction of a value in the range min->max as a value between 0 and 1.
     * For example if min = 10, max = 20 and value = 16, then the result = 0.6.
     */
    public static double calculateFractionInRange(double min, double max, double value) {
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

    /**
     * Calculate a value in the range min->max at the given fraction.
     * For example if min = 10, max = 20 and fraction = 0.6, then the result = 16.
     */
    public static double calculateValueInRange(final double min, final double max, final double fraction) {
        return min + fraction * (max - min);
    }

    /**
     * Calculate the normalized form of a vector (with length 1).
     */
    public static Vector normalize(Vector input) {
        return input.divide(input.euclideanNorm());
    }

    /**
     * Reverse an angle (in degrees).
     */
    public static double reverseAngle(double angle) {
        return 180 - angle;
    }

}
