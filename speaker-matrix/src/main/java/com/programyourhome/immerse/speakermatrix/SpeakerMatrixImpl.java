package com.programyourhome.immerse.speakermatrix;

import java.util.Map;

import org.la4j.Vector;
import org.springframework.stereotype.Component;

import com.programyourhome.immerse.speakermatrix.model.Room;
import com.programyourhome.immerse.speakermatrix.model.Scene;
import com.programyourhome.immerse.speakermatrix.model.Speaker;
import com.programyourhome.immerse.speakermatrix.model.SpeakerMatrixSettings;
import com.programyourhome.immerse.speakermatrix.model.SpeakerVolumes;
import com.programyourhome.immerse.speakermatrix.model.SurroundMode;
import com.programyourhome.immerse.speakermatrix.model.Vector3D;

import one.util.streamex.DoubleStreamEx;
import one.util.streamex.EntryStream;

@Component
public class SpeakerMatrixImpl implements SpeakerMatrix {

    @Override
    public SpeakerVolumes calculateSurroundSound(Scene scene) {
        this.validateScene(scene);
        Map<Integer, Double> speakerAngles = EntryStream.of(scene.getRoom().getSpeakers())
                .mapValues(speaker -> this.calculateAngleInDegrees(scene, speaker))
                .toMap();
        // TODO: decide on algorithm to use for dividing the volumes among the speakers. + make configurable
        // TODO: current algorithm has the quirk that adding extra speakers 'far away' will influence the volume of the otherwise identical scene.
        // But that is also sortof a 'feature': not having enough speakers is a sub-optimal experience anyway with this lib...
        // IDEA: square the volume fraction to focus on closest speakers
        // IDEA: pick the x closest ones and devide evenly according to distance
        // TODO: integrate distance.
        // TODO: integrate multiplier.
        double minAngle = DoubleStreamEx.of(speakerAngles.values()).min().getAsDouble();
        double maxAngle = DoubleStreamEx.of(speakerAngles.values()).max().getAsDouble();
        Map<Integer, Double> volumeMap = EntryStream.of(speakerAngles)
                .mapValues(angle -> this.calculateVolumeFraction(minAngle, maxAngle, angle))
                .toMap();
        return new SpeakerVolumes(volumeMap);
    }

    private double calculateVolumeFraction(double minAngle, double maxAngle, double angle) {
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

    // TODO: again, unit tests!!!!
    public static void main(String[] args) {
        SpeakerMatrixSettings settings = new SpeakerMatrixSettings(SurroundMode.ANGLE_ONLY);
        Vector3D source = new Vector3D(10, 0, 0);
        Vector3D listener = new Vector3D(0, 0, 0);
        Room room = new Room("Test Room", "");
        room.addSpeaker(new Speaker(1, "speaker1", "", 1, new Vector3D(5, 5, 0)));
        room.addSpeaker(new Speaker(2, "speaker2", "", 1, new Vector3D(6, 5, 0)));
        room.addSpeaker(new Speaker(3, "speaker3", "", 1, new Vector3D(7, 5, 0)));
        room.addSpeaker(new Speaker(4, "speaker4", "", 1, new Vector3D(8, 5, 0)));
        room.addSpeaker(new Speaker(5, "speaker5", "", 1, new Vector3D(9, 5, 0)));
        room.addSpeaker(new Speaker(6, "speaker6", "", 1, new Vector3D(10, 5, 0)));
        Scene scene = new Scene(room, listener, source, settings);

        System.out.println(new SpeakerMatrixImpl().calculateSurroundSound(scene));
    }

    private void validateScene(Scene scene) {
        if (scene.getRoom().getSpeakers().isEmpty()) {
            throw new IllegalArgumentException("No speakers configured!");
        }
        // TODO: more validation, like:
        // room not null, validate speaker data
    }

    private double calculateAngleInDegrees(Scene scene, Speaker speakerPojo) {
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

    private Vector normalize(Vector input) {
        return input.divide(input.euclideanNorm());
    }

    /**
     * Calculate the dot product.
     * Correct for double imprecision if the result is out of bounds, so acos will always work on the result.
     */
    private double safeDotProduct(Vector v1, Vector v2) {
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
