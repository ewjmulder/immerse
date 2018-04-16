package com.programyourhome.immerse.audiostreaming.mixer;

import org.pmw.tinylog.Logger;

/**
 * Listener of state changes of a mixer.
 * Defined as functional interface for easy lambda based definitions.
 */
public interface MixerStateListener {

    /**
     * Used by the mixer to send events without the risk of breaking due to an exception at the listener side.
     * Not meant to be overridden by an implementing class, use stateChanged instead.
     */
    public default void stateChangedNoException(MixerState fromState, MixerState toState) {
        try {
            this.stateChanged(fromState, toState);
        } catch (Exception e) {
            Logger.error(e, "Exception while notifying state listener");
        }
    }

    /**
     * Informing the listener that the state of the mixer has just changed from fromState to toState.
     */
    public void stateChanged(MixerState fromState, MixerState toState);

}
