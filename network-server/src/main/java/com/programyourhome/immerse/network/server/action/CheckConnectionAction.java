package com.programyourhome.immerse.network.server.action;

import java.io.ObjectInput;

import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Check the connection to the server.
 */
public class CheckConnectionAction extends Action<Void> {

    @Override
    public Void perform(ImmerseServer server, ObjectInput objectInput) throws Exception {
        // If the code execution comes at this point, everything works properly, so just return.
        return VOID_RETURN_VALUE;
    }

}
