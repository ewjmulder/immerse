package com.programyourhome.immerse.audiostreaming;

import java.util.Arrays;
import java.util.List;

// Using allowed previous states because of enum cannot reference
public enum MixerState {

    NEW(false),
    WARMUP(false, NEW),
    INITIALIZED(true, WARMUP),
    STARTED(true, INITIALIZED),
    STOPPING(false, STARTED),
    STOPPED(false, STOPPING);

    private boolean running;
    private List<MixerState> allowedPreviousStates;

    private MixerState(boolean running, MixerState... allowedPreviousStates) {
        this.running = running;
        this.allowedPreviousStates = Arrays.asList(allowedPreviousStates);
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isAllowedPreviousState(MixerState mixerState) {
        return this.allowedPreviousStates.contains(mixerState);
    }

    public boolean isAllowedNextState(MixerState mixerState) {
        return mixerState.allowedPreviousStates.contains(this);
    }

}
