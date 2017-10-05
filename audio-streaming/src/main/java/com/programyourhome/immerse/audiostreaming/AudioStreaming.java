package com.programyourhome.immerse.audiostreaming;

import java.util.Set;

import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCardToSpeakerConfiguration;

public interface AudioStreaming {

    public void playScenario(Room room, Scenario scenario, Set<SoundCard> soundCards, SoundCardToSpeakerConfiguration cardToSpeakerConfiguration);

}
