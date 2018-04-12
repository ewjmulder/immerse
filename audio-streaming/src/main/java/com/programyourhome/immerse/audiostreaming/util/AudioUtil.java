package com.programyourhome.immerse.audiostreaming.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioUtil {

    private AudioUtil() {
    }

    public static AudioFormat toSigned(AudioFormat format) {
        return new AudioFormat(format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(), true, format.isBigEndian());
    }

    public static AudioInputStream convert(AudioInputStream originalStream, AudioFormat newFormat) {
        if (!AudioSystem.isConversionSupported(newFormat, originalStream.getFormat())) {
            throw new IllegalArgumentException("Conversion to desired format is not supported");
        }
        return AudioSystem.getAudioInputStream(newFormat, originalStream);
    }

}
