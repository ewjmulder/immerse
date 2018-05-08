package com.programyourhome.immerse.network.server.action;

import java.io.ObjectInput;

import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Start the mixer.
 */
public class StartMixerAction extends Action<Void> {

    @Override
    public Void perform(ImmerseServer server, ObjectInput objectInput) {
        if (!server.hasMixer()) {
            throw new IllegalStateException("Server has no mixer, starting is not possible");
        }
        server.getMixer().start();
        return VOID_RETURN_VALUE;
    }

}
