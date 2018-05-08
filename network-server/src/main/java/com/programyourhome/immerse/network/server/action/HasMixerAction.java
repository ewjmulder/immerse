package com.programyourhome.immerse.network.server.action;

import java.io.ObjectInput;

import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Does the server has a mixer or not.
 */
public class HasMixerAction extends Action<Boolean> {

    @Override
    public Boolean perform(ImmerseServer server, ObjectInput objectInput) throws Exception {
        return server.hasMixer();
    }

}
