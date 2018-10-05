package com.programyourhome.immerse.toolbox.speakers.algorithms.volumeratios;

import com.programyourhome.immerse.domain.AbstractDynamicData;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Serialization;
import com.programyourhome.immerse.domain.location.Vector3D;
import com.programyourhome.immerse.domain.location.dynamic.DynamicLocation;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;
import com.programyourhome.immerse.domain.speakers.algorithms.volumeratios.VolumeRatiosAlgorithm;

public abstract class AbstractLocationBasedVolumeRatiosAlgorithm extends AbstractDynamicData<SpeakerVolumeRatios> implements VolumeRatiosAlgorithm {

    private static final long serialVersionUID = Serialization.VERSION;

    private final Room room;
    private final DynamicLocation sourceLocation;
    private final DynamicLocation listenerLocation;

    public AbstractLocationBasedVolumeRatiosAlgorithm(Room room, DynamicLocation sourceLocation, DynamicLocation listenerLocation) {
        this.room = room;
        this.sourceLocation = sourceLocation;
        this.listenerLocation = listenerLocation;
    }

    @Override
    public void nextPlaybackStarted() {
        super.nextPlaybackStarted();
        this.sourceLocation.nextPlaybackStarted();
        this.listenerLocation.nextPlaybackStarted();
    }

    @Override
    public SpeakerVolumeRatios getCurrentValue() {
        // Get the source and listener location from the configured dynamic location objects.
        Vector3D source = this.sourceLocation.getCurrentValue();
        Vector3D listener = this.listenerLocation.getCurrentValue();
        return this.calculateVolumeRatios(this.room, source, listener);
    }

    protected abstract SpeakerVolumeRatios calculateVolumeRatios(Room room, Vector3D sourceLocation, Vector3D listenerLocation);

}
