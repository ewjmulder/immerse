package com.programyourhome.immerse.domain.speakers.algorithms.volumeratios;

import org.la4j.Vector;

import com.programyourhome.immerse.domain.Scene;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

public interface SpeakerVolumeRatiosAlgorithm {

    public SpeakerVolumeRatios calculateVolumeRatios(Scene scene);

    default double calculateAngleInDegrees(Scene scene, Speaker speakerPojo) {
        Vector listener = scene.getListener().toLa4j();
        Vector source = scene.getSource().toLa4j();
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

    default double calculateAngleInDegrees(Vector base, Vector target1, Vector target2) {
        Vector baseToTarget1 = target1.subtract(base);
        Vector baseToTarget1Direction = this.normalize(baseToTarget1);
        Vector baseToTarget2 = target2.subtract(base);
        Vector baseToTarget2Direction = this.normalize(baseToTarget2);
        double dotProduct = this.safeDotProduct(baseToTarget1Direction, baseToTarget2Direction);
        double angleInRadians = Math.acos(dotProduct);
        return Math.toDegrees(angleInRadians);
    }

    public static void main(String[] args) {
        Vector listener = Vector.fromArray(new double[] { 5, 5, 5 });
        Vector source = Vector.fromArray(new double[] { 10, 5, 5 });
        Vector speaker1 = Vector.fromArray(new double[] { 10, 4, 4 });
        Vector speaker2 = Vector.fromArray(new double[] { 10, 7, 7 });
        Vector speaker3 = Vector.fromArray(new double[] { 10, 8, 2 });

        SpeakerVolumeRatiosAlgorithm algo = new SpeakerVolumeRatiosAlgorithm() {
            @Override
            public SpeakerVolumeRatios calculateVolumeRatios(Scene scene) {
                // TODO Auto-generated method stub
                return null;
            }
        };
        System.out.println(algo.calculateAngleInDegrees(listener, source, speaker1));
        System.out.println(algo.calculateAngleInDegrees(listener, source, speaker2));
        System.out.println(algo.calculateAngleInDegrees(listener, source, speaker3));
    }

    default Vector normalize(Vector input) {
        return input.divide(input.euclideanNorm());
    }

    /**
     * Calculate the dot product.
     * Correct for double imprecision if the result is out of bounds, so acos will always work on the result.
     */
    default double safeDotProduct(Vector v1, Vector v2) {
        double dotProduct = v1.innerProduct(v2);
        if (dotProduct > 1) {
            dotProduct = 1;
        }
        if (dotProduct < -1) {
            dotProduct = -1;
        }
        return dotProduct;
    }

    default double reverseAngle(double angle) {
        return 180 - angle;
    }

}
