package com.programyourhome.immerse.domain.location.dynamic.math;

import java.util.function.Function;

public interface MathFunction extends Function<Long, Double> {

    @Override
    default Double apply(Long millisSinceStart) {
        return getValue(millisSinceStart);
    }

    public double getValue(long millisSinceStart);

}
