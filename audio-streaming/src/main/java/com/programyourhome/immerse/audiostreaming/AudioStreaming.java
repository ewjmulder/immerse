package com.programyourhome.immerse.audiostreaming;

import java.util.Set;

import com.programyourhome.immerse.audiostreaming.model.Scenario;
import com.programyourhome.immerse.audiostreaming.model.soundcard.SoundCard;
import com.programyourhome.immerse.audiostreaming.model.soundcard.SoundCardToSpeakerConfiguration;
import com.programyourhome.immerse.speakermatrix.model.Room;

public interface AudioStreaming {

    public void playScenario(Room room, Scenario scenario, Set<SoundCard> soundCards, SoundCardToSpeakerConfiguration cardToSpeakerConfiguration);

}
