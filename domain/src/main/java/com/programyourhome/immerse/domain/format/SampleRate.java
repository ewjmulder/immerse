package com.programyourhome.immerse.domain.format;

import java.util.Arrays;

/**
 * Enum for sample rate with the most common sample rates as options.
 * Slightly simplified numbering, e.g. 44K instead of 44100.
 */
public enum SampleRate {

    RATE_8K(8000),
    RATE_11K(11025),
    RATE_16K(16000),
    RATE_22K(22050),
    RATE_32K(32000),
    RATE_44K(44100),
    RATE_48K(48000);

    private int numberOfSamplesPerSecond;

    private SampleRate(final int numberOfSamplesPerSecond) {
        this.numberOfSamplesPerSecond = numberOfSamplesPerSecond;
    }

    public int getNumberOfSamplesPerSecond() {
        return this.numberOfSamplesPerSecond;
    }

    public static SampleRate fromNumberOfSamplesPerSecond(float sampleRate) {
        return Arrays.stream(SampleRate.values())
                .filter(rate -> rate.getNumberOfSamplesPerSecond() == sampleRate)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sample rate: '" + sampleRate + "' not supported"));
    }

    public static SampleRate phoneQuality() {
        return RATE_8K;
    }

    public static SampleRate cdQuality() {
        return RATE_44K;
    }

    public static SampleRate dvdQuality() {
        return RATE_48K;
    }

}
