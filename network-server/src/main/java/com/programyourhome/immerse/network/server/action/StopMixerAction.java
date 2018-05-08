package com.programyourhome.immerse.network.server.action;

import java.io.ObjectInput;

import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Stop the mixer.
 */
public class StopMixerAction extends Action<Void> {

    @Override
    public Void perform(ImmerseServer server, ObjectInput objectInput) {
        if (!server.hasMixer()) {
            throw new IllegalStateException("Server has no mixer, stopping is not possible");
        }
        server.getMixer().stop();
        server.setMixer(null);
        return VOID_RETURN_VALUE;
    }

}
