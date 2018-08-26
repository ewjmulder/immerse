package com.programyourhome.immerse.network.server.action;

import java.io.ObjectInput;

import com.programyourhome.immerse.domain.ImmerseSettings;
import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Get the settings.
 */
public class GetSettingsAction extends Action<ImmerseSettings> {

    @Override
    public ImmerseSettings perform(ImmerseServer server, ObjectInput objectInput) {
        if (!server.hasMixer()) {
            throw new IllegalStateException("Server has no mixer, no settings to get");
        }
        return server.getMixer().getSettings();
    }

}
