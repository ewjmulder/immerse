package com.programyourhome.immerse.audiostreaming.model;

import com.programyourhome.immerse.audiostreaming.model.stopcriterium.StopCriterium;

public class AudioStreamingSettings {

    private StopCriterium stopCriterium;

    public AudioStreamingSettings(StopCriterium stopCriterium) {
        this.stopCriterium = stopCriterium;
    }

    public StopCriterium getStopCriterium() {
        return this.stopCriterium;
    }

}
