package com.programyourhome.immerse.audiostreaming.format;

import java.util.Arrays;

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
