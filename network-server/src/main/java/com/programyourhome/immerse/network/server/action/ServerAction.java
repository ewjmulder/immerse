package com.programyourhome.immerse.network.server.action;

/**
 * All possible server actions and the corresponding action implementations.
 */
public enum ServerAction {

    /** Create a new mixer. */
    CHECK_CONNECTION(new CheckConnectionAction(), true),
    /** Create a new mixer. */
    HAS_MIXER(new HasMixerAction(), true),
    /** Create a new mixer. */
    CREATE_MIXER(new CreateMixerAction(), false),
    /** Start the mixer. */
    START_MIXER(new StartMixerAction(), false),
    /** Get the settings. */
    GET_SETTINGS(new GetSettingsAction(), true),
    /** Play a scenario. */
    PLAY_SCENARIO(new PlayScenarioAction(), true),
    /** Wait for a playback to finish. */
    WAIT_FOR_PLAYBACK(new WaitForPlaybackAction(), true),
    /** Stop playback. */
    STOP_PLAYBACK(new StopPlaybackAction(), true),
    /** Stop the mixer. */
    STOP_MIXER(new StopMixerAction(), false);

    private Action<?> action;
    private boolean canRunAsync;

    private ServerAction(Action<?> action, boolean canRunAsync) {
        this.action = action;
        this.canRunAsync = canRunAsync;
    }

    public Action<?> getAction() {
        return this.action;
    }

    public boolean isCanRunAsync() {
        return this.canRunAsync;
    }

}
