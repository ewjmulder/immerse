package com.programyourhome.immerse.audiostreaming.model.dynamiclocation;

import java.util.function.Function;

import com.programyourhome.immerse.speakermatrix.model.Vector3D;

public class MathFunctionDynamicLocation implements DynamicLocation {

    private Function<Long, Vector3D> function;

    // TODO: refactor this to make math sanity!

    // public MathFunctionDynamicLocation(MathFunction xFunction, MathFunction yFunction, MathFunction zFunction) {
    // this.function = function;
    // }

    @Override
    public Vector3D getLocation(long millisSinceStart) {
        return this.function.apply(millisSinceStart);
    }

}
