package com.programyourhome.immerse.audiostreaming;

import java.util.Set;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCardToSpeakerConfiguration;
import com.programyourhome.immerse.speakermatrix.SpeakerMatrix;

@Component
public class AudioStreamingImpl implements AudioStreaming {

    @Inject
    private Executor scenarioExecutor;

    @Inject
    private SpeakerMatrix speakerMatrix;

    @Override
    public void playScenario(Room room, Scenario scenario, Set<SoundCard> soundCards, SoundCardToSpeakerConfiguration soundCardToSpeakerConfiguration) {
        this.scenarioExecutor.execute(() -> new ScenarioPlayer(this.speakerMatrix, soundCards, room, scenario, soundCardToSpeakerConfiguration).play());
    }

}
