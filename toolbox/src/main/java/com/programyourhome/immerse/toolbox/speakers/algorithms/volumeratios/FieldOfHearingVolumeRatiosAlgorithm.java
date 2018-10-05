package com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios;

import com.programyourhome.immerse.domain.Factory;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;
import com.programyourhome.immerse.toolbox.util.MathUtil;

import one.util.streamex.EntryStream;

/**
 * Volume ratios algorithm that takes a certain 'field of hearing': a cone shaped area
 * with the tip at the listener and in the direction of the source with a certain 'cutoff' angle.
 * A speaker within the cone can produce sound for the source, the closer to the center, the louder it will be.
 * Speakers outside the cone (so having a bigger angle than the 'cutoff' angle) will remain silent.
 * So it will use the angle of the speaker as measurement for relative volume.
 *
 * NB: The cutoff angle should not be smaller than twice the angle between speakers, otherwise
 * there can be 'silent spots' in the room, even though the scenario produces sound.
 */
public class FieldOfHearingVolumeRatiosAlgorithm extends AbstractLocationBasedVolumeRatiosAlgorithm {

    private static final long serialVersionUID = Serialization.VERSION;

    // The default 'cutoff' angle.
    public static final double DEFAULT_CUTOFF_ANGLE = 45;

    private final double cutoffAngle;

    public FieldOfHearingVolumeRatiosAlgorithm(Room room, DynamicLocation sourceLocation, DynamicLocation listenerLocation) {
        this(room, sourceLocation, listenerLocation, DEFAULT_CUTOFF_ANGLE);
    }

    public FieldOfHearingVolumeRatiosAlgorithm(Room room, DynamicLocation sourceLocation, DynamicLocation listenerLocation, double cutoffAngle) {
        super(room, sourceLocation, listenerLocation);
        this.cutoffAngle = cutoffAngle;
    }

    @Override
    protected SpeakerVolumeRatios calculateVolumeRatios(Room room, Vector3D sourceLocation, Vector3D listenerLocation) {
        return new SpeakerVolumeRatios(EntryStream.of(room.getSpeakers())
                .mapValues(speaker -> MathUtil.calculateAngleInDegrees(sourceLocation, listenerLocation, speaker))
                // For speakers inside the 'field of hearing', a low angle should be a high volume ratio and vice versa.
                // For speakers not inside the 'field of hearing', it's just 0.
                .mapValues(angle -> angle <= this.cutoffAngle ? this.cutoffAngle - angle : 0.0)
                .toMap());
    }

    public static Factory<VolumeRatiosAlgorithm> fieldOfHearing(Room room, Factory<DynamicLocation> sourceLocation, Factory<DynamicLocation> listenerLocation) {
        return fieldOfHearing(room, sourceLocation, listenerLocation, DEFAULT_CUTOFF_ANGLE);
    }

    public static Factory<VolumeRatiosAlgorithm> fieldOfHearing(Room room, Factory<DynamicLocation> sourceLocation, Factory<DynamicLocation> listenerLocation,
            double cutoffAngle) {
        return new Factory<VolumeRatiosAlgorithm>() {
            private static final long serialVersionUID = Serialization.VERSION;

            @Override
            public VolumeRatiosAlgorithm create() {
                return new FieldOfHearingVolumeRatiosAlgorithm(room, sourceLocation.create(), listenerLocation.create(), cutoffAngle);
            }
        };
    }

}
