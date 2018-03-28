package com.programyourhome.immerse.audiostreaming.format;

import java.util.Arrays;

public enum SampleSize {

    ONE_BYTE(1, false),
    TWO_BYTES(2, true);

    private int numberOfBytes;
    private boolean signed;

    private SampleSize(int numberOfBytes, boolean signed) {
        this.numberOfBytes = numberOfBytes;
        this.signed = signed;
    }

    public int getNumberOfBytes() {
        return this.numberOfBytes;
    }

    public int getNumberOfBits() {
        return this.numberOfBytes * 8;
    }

    public boolean isSigned() {
        return this.signed;
    }

    public static SampleSize fromNumberOfBytes(int numberOfBytes) {
        return Arrays.stream(SampleSize.values())
                .filter(size -> size.getNumberOfBytes() == numberOfBytes)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Number of bytes: '" + numberOfBytes + "' not supported"));
    }

    public static SampleSize fromNumberOfBits(int numberOfBits) {
        if (numberOfBits % 8 != 0) {
            throw new IllegalArgumentException("Number of bits: '" + numberOfBits + "' not supported");
        }
        return fromNumberOfBytes(numberOfBits / 8);
    }

}
