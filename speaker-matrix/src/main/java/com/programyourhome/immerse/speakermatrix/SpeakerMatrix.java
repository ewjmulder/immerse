package com.programyourhome.immerse.speakermatrix;

import com.programyourhome.immerse.speakermatrix.model.Scene;
import com.programyourhome.immerse.speakermatrix.model.SpeakerVolumes;

public interface SpeakerMatrix {

    public SpeakerVolumes calculateSurroundSound(Scene scene);

}
