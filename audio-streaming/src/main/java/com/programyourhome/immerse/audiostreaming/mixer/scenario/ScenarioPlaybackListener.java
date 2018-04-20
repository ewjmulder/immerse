package com.programyourhome.immerse.audiostreaming.mixer.scenario;

import java.util.UUID;
import java.util.function.Consumer;

import org.pmw.tinylog.Logger;

/**
 * Listener interface for the scenario lifecycle inside a mixer.
 * Notifications contain the playback id, that is received when calling playScenario on a mixer.
 * This is the only unique identifier for a certain playback, since scenario objects can be reused.
 */
public interface ScenarioPlaybackListener {

    /**
     * Scenario playback has been started.
     * Will also be called after a restart.
     */
    public void scenarioStarted(UUID playbackId);

    /**
     * Scenario playback has been restarted.
     */
    public void scenarioRestarted(UUID playbackId);

    /**
     * Scenario playback has been stopped.
     */
    public void scenarioStopped(UUID playbackId);

    /**
     * Used by the mixer to send events without the risk of breaking due to an exception at the listener side.
     * Not meant to be overridden by an implementing class, use stateChanged instead.
     */
    public default void scenarioEventNoException(Consumer<UUID> eventConsumer, UUID playbackId) {
        try {
            eventConsumer.accept(playbackId);
        } catch (Exception e) {
            Logger.error(e, "Exception while notifying scenario listener");
        }
    }

}
