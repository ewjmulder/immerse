package com.programyourhome.immerse.audiostreaming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScenarioLoopContext {

    private final int amountOfFramesNeeded;
    private final Set<ActiveScenario> scenariosToRemove;
    private final Set<ActiveScenario> scenariosToRestart;
    private final Map<SoundCardStream, byte[]> soundCardOutputBuffers;

    public ScenarioLoopContext(int amountOfFramesNeeded) {
        this.amountOfFramesNeeded = amountOfFramesNeeded;
        this.scenariosToRemove = new HashSet<>();
        this.scenariosToRestart = new HashSet<>();
        this.soundCardOutputBuffers = new HashMap<>();
    }

    public int getAmountOfFramesNeeded() {
        return this.amountOfFramesNeeded;
    }

    public Set<ActiveScenario> getScenariosToRemove() {
        return this.scenariosToRemove;
    }

    public Set<ActiveScenario> getScenariosToRestart() {
        return this.scenariosToRestart;
    }

    public Map<SoundCardStream, byte[]> getSoundCardOutputBuffers() {
        return this.soundCardOutputBuffers;
    }

    public void addScenarioToRemove(ActiveScenario scenario) {
        this.scenariosToRemove.add(scenario);
    }

    public void addScenarioToRestart(ActiveScenario scenario) {
        this.scenariosToRestart.add(scenario);
    }

}
