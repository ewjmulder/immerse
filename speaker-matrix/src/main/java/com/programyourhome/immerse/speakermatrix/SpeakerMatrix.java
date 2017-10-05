package com.programyourhome.immerse.speakermatrix;

import com.programyourhome.immerse.domain.Scene;
import com.programyourhome.immerse.domain.speakers.SpeakerVolumes;

public interface SpeakerMatrix {

    public SpeakerVolumes calculateSurroundSound(Scene scene);

}
