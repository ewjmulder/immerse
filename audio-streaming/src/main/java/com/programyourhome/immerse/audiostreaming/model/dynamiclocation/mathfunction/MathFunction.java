package com.programyourhome.immerse.audiostreaming.model.dynamiclocation.mathfunction;

import java.util.function.Function;

public interface MathFunction extends Function<Long, Double> {

    @Override
    default Double apply(Long millisSinceStart) {
        return getValue(millisSinceStart);
    }

    public double getValue(long millisSinceStart);

}
