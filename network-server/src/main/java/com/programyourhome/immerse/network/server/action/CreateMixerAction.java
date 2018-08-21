package com.programyourhome.immerse.network.server.action;

import java.io.IOException;
import java.io.ObjectInput;

import com.programyourhome.immerse.audiostreaming.mixer.ImmerseMixer;
import com.programyourhome.immerse.audiostreaming.mixer.ImmerseSettings;
import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Create the mixer.
 */
public class CreateMixerAction extends Action<Void> {

    @Override
    public Void perform(ImmerseServer server, ObjectInput objectInput) throws ClassNotFoundException, IOException {
        ImmerseSettings settings = this.read(objectInput, ImmerseSettings.class);

        if (server.hasMixer()) {
            throw new IllegalStateException("Server already has a mixer, overriding is not possible. First stop, then re-create.");
        }

        ImmerseMixer mixer = new ImmerseMixer(settings);
        mixer.initialize();
        server.setMixer(mixer);
        return VOID_RETURN_VALUE;
    }

}
