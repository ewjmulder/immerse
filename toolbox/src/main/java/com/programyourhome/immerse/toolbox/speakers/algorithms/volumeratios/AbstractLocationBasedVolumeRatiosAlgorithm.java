package com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios;

import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

public abstract class AbstractLocationBasedVolumeRatiosAlgorithm implements VolumeRatiosAlgorithm {

    private static final long serialVersionUID = Serialization.VERSION;

    private final DynamicLocation sourceLocation;
    private final DynamicLocation listenerLocation;

    public AbstractLocationBasedVolumeRatiosAlgorithm(DynamicLocation sourceLocation, DynamicLocation listenerLocation) {
        this.sourceLocation = sourceLocation;
        this.listenerLocation = listenerLocation;
    }

    @Override
    public SpeakerVolumeRatios calculateVolumeRatios(Room room, long millisSinceStart) {
        // Get the source and listener location from the configured dynamic location objects.
        Vector3D source = this.sourceLocation.getLocation(millisSinceStart);
        Vector3D listener = this.listenerLocation.getLocation(millisSinceStart);
        return this.calculateVolumeRatios(room, source, listener);
    }

    protected abstract SpeakerVolumeRatios calculateVolumeRatios(Room room, Vector3D sourceLocation, Vector3D listenerLocation);

}
