package com.programyourhome.immerse.network.server.action;

/**
 * All possible server actions and the corresponding action implementations.
 */
public enum ServerAction {

    /** Create a new mixer. */
    CHECK_CONNECTION(new CheckConnectionAction()),
    /** Create a new mixer. */
    HAS_MIXER(new HasMixerAction()),
    /** Create a new mixer. */
    CREATE_MIXER(new CreateMixerAction()),
    /** Start the mixer. */
    START_MIXER(new StartMixerAction()),
    /** Get the settings. */
    GET_SETTINGS(new GetSettingsAction()),
    /** Play a scenario. */
    PLAY_SCENARIO(new PlayScenarioAction()),
    /** Wait for a playback to finish. */
    WAIT_FOR_PLAYBACK(new WaitForPlaybackAction()),
    /** Stop playback. */
    STOP_PLAYBACK(new StopPlaybackAction()),
    /** Stop the mixer. */
    STOP_MIXER(new StopMixerAction());

    private Action<?> action;

    private ServerAction(Action<?> action) {
        this.action = action;
    }

    public Action<?> getAction() {
        return this.action;
    }

}
