package com.programyourhome.immerse.domain.format;

import java.util.Arrays;

/**
 * Enum for recording mode: mono or stereo.
 * More than 2 channels are not supported.
 */
public enum RecordingMode {

    MONO(1),
    STEREO(2);

    private int numberOfChannels;

    private RecordingMode(final int numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public int getNumberOfChannels() {
        return this.numberOfChannels;
    }

    public static RecordingMode fromNumberOfChannels(int numberOfChannels) {
        return Arrays.stream(RecordingMode.values())
                .filter(mode -> mode.getNumberOfChannels() == numberOfChannels)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Number of channels: '" + numberOfChannels + "' not supported"));
    }

}
