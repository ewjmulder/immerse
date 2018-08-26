package com.programyourhome.immerse.audiostreaming.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * Util methods for audio streams and formats.
 */
public class AudioUtil {

    private AudioUtil() {
    }

    /**
     * Create a new audio format based on the given format, but set the signed-ness to true.
     */
    public static AudioFormat toSigned(AudioFormat format) {
        return new AudioFormat(format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(), true, format.isBigEndian());
    }

    /**
     * Perform a conversion from the given audio stream into the new format.
     * This method leaves the heavy lifting up to the Java Sound API by invoking the right methods on AudioSystem.
     */
    public static AudioInputStream convert(AudioInputStream originalStream, AudioFormat newFormat) {
        if (!AudioSystem.isConversionSupported(newFormat, originalStream.getFormat())) {
            throw new IllegalArgumentException("Conversion to desired format is not supported");
        }
        return AudioSystem.getAudioInputStream(newFormat, originalStream);
    }

    /**
     * Assert the audio format is signed.
     */
    public static void assertSigned(ImmerseAudioFormat format) {
        if (!format.isSigned()) {
            throw new IllegalArgumentException("An unsiged audio format is not supported");
        }
    }

}
