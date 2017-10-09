package com.programyourhome.immerse.domain.speakers.algorithms;

import org.la4j.Vector;

import com.programyourhome.immerse.domain.Scene;
import com.programyourhome.immerse.domain.speakers.Speaker;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

public interface SpeakerVolumesAlgorithm {

    public SpeakerVolumes calculateSpeakerVolumes(Scene scene);

    default double calculateVolumeFraction(double minAngle, double maxAngle, double angle) {
        double angleDiff = maxAngle - minAngle;
        double volumeFraction;
        if (angleDiff == 0) {
            // Special case: just 1 speaker or all speakers are at exactly the same angle from the source).
            volumeFraction = 1;
        } else {
            volumeFraction = (maxAngle - angle) / angleDiff;
        }
        return volumeFraction;
    }

    default double calculateAngleInDegrees(Scene scene, Speaker speakerPojo) {
        Vector listener = scene.getListener().toLa4j();
        Vector source = scene.getSource().toLa4j();
        Vector speaker = speakerPojo.getVectorLa4j();
        Vector listenerToSource = source.subtract(listener);
        Vector listenerToSourceDirection = this.normalize(listenerToSource);
        Vector listenerToSpeaker = speaker.subtract(listener);
        Vector listenerToSpeakerDirection = this.normalize(listenerToSpeaker);
        double dotProduct = this.safeDotProduct(listenerToSourceDirection, listenerToSpeakerDirection);
        double angleInRadians = Math.acos(dotProduct);
        return Math.toDegrees(angleInRadians);
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

}
