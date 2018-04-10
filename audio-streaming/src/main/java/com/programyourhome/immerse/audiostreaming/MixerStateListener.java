package com.programyourhome.immerse.audiostreaming;

public interface MixerStateListener {

    public default void stateChangedNoException(MixerState fromState, MixerState toState) {
        try {
            this.stateChanged(fromState, toState);
        } catch (Exception e) {
            // TODO: logging
            e.printStackTrace();
        }
    }

    public void stateChanged(MixerState fromState, MixerState toState);

}
