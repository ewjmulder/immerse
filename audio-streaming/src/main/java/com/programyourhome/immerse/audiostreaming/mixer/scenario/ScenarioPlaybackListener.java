package com.programyourhome.immerse.audiostreaming.mixer.scenario;

import java.util.function.Consumer;

import com.programyourhome.immerse.domain.Scenario;

public interface ScenarioPlaybackListener {

    public void scenarioStarted(Scenario scenario);

    // Restarted will also trigger started
    public void scenarioRestarted(Scenario scenario);

    public void scenarioStopped(Scenario scenario);

    public default void scenarioEventNoException(Consumer<Scenario> eventConsumer, Scenario scenario) {
        try {
            eventConsumer.accept(scenario);
        } catch (Exception e) {
            // TODO: proper logging
            e.printStackTrace();
        }
    }

}
