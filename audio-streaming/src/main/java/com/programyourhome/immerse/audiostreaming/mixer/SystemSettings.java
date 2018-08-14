package com.programyourhome.immerse.audiostreaming.mixer;

import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;

/**
 * This class holds all Immerse system wide settings.
 * It's design is not really according to any nice pattern, but is very pragmatic
 * statically accessible, so it will be easy to get settings from any piece of code
 * without having to pass a reference to it all over the place.
 *
 * TODO: make configurable, document usage (should be set before mixer starts and stay the same for 1 mixer 'lifetime').
 * TODO: refine this further, see #74
 */
public class SystemSettings {

    private SystemSettings() {
    }

    public static int SOUND_CARD_BUFFER_MILLIS = 30;
    public static int STEP_PACE_MILLIS = 5;
    public static int AUDIO_INPUT_BUFFER_MILLIS_LIVE = STEP_PACE_MILLIS * 2;
    public static int SCENARIO_INPUT_BUFFER_MILLIS_NON_LIVE = STEP_PACE_MILLIS * 4;

    public static int calculateScenarioInputBufferSize(ImmerseAudioFormat format, boolean live) {
        int bufferSizeMillis = live ? AUDIO_INPUT_BUFFER_MILLIS_LIVE : SCENARIO_INPUT_BUFFER_MILLIS_NON_LIVE;
        // Take an (approximate) buffer size for the amount of millis we want to buffer.
        int bufferSizeBytes = bufferSizeMillis * (int) format.getNumberOfBytesPerMilli();
        // Cut the buffer size off at an exact amount of frames to avoid issues when reading from the underlying AudioInputStream.
        bufferSizeBytes -= bufferSizeBytes % format.getNumberOfBytesPerFrame();
        return bufferSizeBytes;
    }

    public static int WAIT_FOR_PREDICATE = 5;
    public static int TRIGGER_MINOR_GC_THRESHOLD_KB = 1000;

}
