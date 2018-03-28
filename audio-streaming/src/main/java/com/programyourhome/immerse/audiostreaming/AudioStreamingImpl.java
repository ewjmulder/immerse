package com.programyourhome.immerse.audiostreaming;

import java.util.Set;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.programyourhome.immerse.domain.Room;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.domain.audio.soundcard.SoundCard;

@Component
public class AudioStreamingImpl implements AudioStreaming {

    @Inject
    private Executor scenarioExecutor;

    @Override
    public void playScenario(Room room, Scenario scenario, Set<SoundCard> soundCards) {
        // this.scenarioExecutor.execute(() -> new ScenarioPlayer(soundCards, room, scenario).play());
    }

}
