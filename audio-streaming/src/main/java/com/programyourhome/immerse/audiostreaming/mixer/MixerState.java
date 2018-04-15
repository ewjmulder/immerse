package com.programyourhome.immerse.audiostreaming.mixer;

import java.util.Arrays;
import java.util.List;

/**
 * Enum for possible mixer states, used to keep track of the startup and shutdown process of a mixer.
 * Technical note: using allowed previous states instead of next states because of enum cannot reference ahead,
 * but isAllowedNextState is possible after creation by reverse checking.
 */
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
