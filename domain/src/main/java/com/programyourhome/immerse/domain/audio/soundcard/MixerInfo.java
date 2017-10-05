package com.programyourhome.immerse.domain.audio.soundcard;

import javax.sound.sampled.Mixer;

public class MixerInfo extends Mixer.Info {

    // Making public constructor
    public MixerInfo(String name, String vendor, String description, String version) {
        super(name, vendor, description, version);
    }

}
