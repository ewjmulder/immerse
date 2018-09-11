package com.programyourhome.immerse.toolbox.util;

public final class ValueRangeUtil {

    private ValueRangeUtil() {
    }

    public static double calculateFractionInRange(final double fraction, final double minValue, final double maxValue) {
        return minValue + fraction * (maxValue - minValue);
    }

    public static double convertValueFromRangeToRange(final double value, final double minValueFrom, final double maxValueFrom,
            final double minValueTo, final double maxValueTo) {
        final double fraction = (value - minValueFrom) / (maxValueFrom - minValueFrom);
        return calculateFractionInRange(fraction, minValueTo, maxValueTo);
    }

}