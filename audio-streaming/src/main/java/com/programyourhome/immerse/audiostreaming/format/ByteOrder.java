package com.programyourhome.immerse.audiostreaming.format;

/**
 * Enum for byte order.
 * There is also java.nio.ByteOrder, but that is not serializable.
 */
public enum ByteOrder {

    /**
     * Constant denoting little-endian byte order. In this order, the bytes of
     * a multibyte value are ordered from least significant to most
     * significant.
     */
    LITTLE_ENDIAN,
    /**
     * Constant denoting big-endian byte order. In this order, the bytes of a
     * multibyte value are ordered from most significant to least significant.
     */
    BIG_ENDIAN;

    public boolean isLittleEndian() {
        return this == ByteOrder.LITTLE_ENDIAN;
    }

    public boolean isBigEndian() {
        return this == ByteOrder.BIG_ENDIAN;
    }

    public static ByteOrder fromLittleEndian(boolean littleEndian) {
        return littleEndian ? ByteOrder.LITTLE_ENDIAN : BIG_ENDIAN;
    }

    public static ByteOrder fromBigEndian(boolean bigEndian) {
        return bigEndian ? BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

}
