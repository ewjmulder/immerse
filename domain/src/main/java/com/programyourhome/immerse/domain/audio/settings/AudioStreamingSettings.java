package com.programyourhome.immerse.domain.audio.settings;

import com.programyourhome.immerse.domain.audio.stopcriterium.StopCriterium;

public class AudioStreamingSettings {

    private StopCriterium stopCriterium;

    public AudioStreamingSettings(StopCriterium stopCriterium) {
        this.stopCriterium = stopCriterium;
    }

    public StopCriterium getStopCriterium() {
        return this.stopCriterium;
    }

}
