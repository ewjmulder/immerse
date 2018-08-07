package com.programyourhome.immerse.network.server.action;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.Collection;
import java.util.HashSet;

import com.programyourhome.immerse.audiostreaming.mixer.ImmerseMixer;
import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.format.ImmerseAudioFormat;
import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Create the mixer.
 */
public class CreateMixerAction extends Action<Void> {

    @Override
    public Void perform(ImmerseServer server, ObjectInput objectInput) throws ClassNotFoundException, IOException {
        Room room = this.read(objectInput, Room.class);
        Collection<SoundCard> soundCards = this.readCollection(objectInput, SoundCard.class);
        ImmerseAudioFormat outputAudioFormat = this.read(objectInput, ImmerseAudioFormat.class);

        if (server.hasMixer()) {
            throw new IllegalStateException("Server already has a mixer, overriding is not possible. First stop, then re-create.");
        }
        ImmerseMixer mixer = new ImmerseMixer(room, new HashSet<>(soundCards), outputAudioFormat);
        mixer.initialize();
        server.setMixer(mixer);
        return VOID_RETURN_VALUE;
    }

}
