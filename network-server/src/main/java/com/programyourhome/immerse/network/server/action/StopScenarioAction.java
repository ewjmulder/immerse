package com.programyourhome.immerse.network.server.action;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.UUID;

import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Play a scenario on the mixer.
 */
public class StopScenarioAction extends Action<Void> {

    @Override
    public Void perform(ImmerseServer server, ObjectInput objectInput) throws ClassNotFoundException, IOException {
        UUID playbackId = this.read(objectInput, UUID.class);

        if (!server.hasMixer()) {
            throw new IllegalStateException("Server does not have a mixer, stopping a scenario is not possible");
        }
        server.getMixer().stopScenarioPlayback(playbackId);
        return VOID_RETURN_VALUE;
    }

}
