package com.programyourhome.immerse.domain.speakers.algorithms.normalize;

import com.programyourhome.immerse.domain.speakers.SpeakerVolumeRatios;

/**
 * Algorithm that can turn a series of speaker volumes into values between 0 and 1 for each speaker.
 */
public interface NormalizeAlgorithm {

    /**
     * Provide the input: the speaker volume ratios. Should be called first.
     */
    public void setSpeakerVolumeRatios(SpeakerVolumeRatios speakerVolumeRatios);

    /**
     * Calculate the normalized volume for a specific speaker.
     * Can be called as many times as needed after setting the speaker volume ratios.
     */
    public double calculateVolumeFraction(int speakerId);

    public static NormalizeAlgorithm maxSum(double maxSum) {
        return new MaxSumNormalizeAlgorithm(maxSum);
    }

    public static NormalizeAlgorithm fractional() {
        return new FractionalNormalizeAlgorithm();
    }

}
