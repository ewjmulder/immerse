package com.programyourhome.immerse.audiostreaming.model.soundcard;

import javax.sound.sampled.Mixer;

public class MixerInfo extends Mixer.Info {

    // Making public constructor
    public MixerInfo(String name, String vendor, String description, String version) {
        super(name, vendor, description, version);
    }

}
