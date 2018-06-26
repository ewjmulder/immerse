package com.programyourhome.immerse.network.server.action;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.UUID;

import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Play playback of a scenario on the mixer.
 */
public class StopPlaybackAction extends Action<Void> {

    @Override
    public Void perform(ImmerseServer server, ObjectInput objectInput) throws ClassNotFoundException, IOException {
        UUID playbackId = this.read(objectInput, UUID.class);

        if (!server.hasMixer()) {
            throw new IllegalStateException("Server does not have a mixer, stopping playback is not possible");
        }
        server.getMixer().stopScenarioPlayback(playbackId);
        return VOID_RETURN_VALUE;
    }

}
