package com.programyourhome.immerse.audiostreaming.mixer.warmup;

import java.util.Map;

import com.programyourhome.immerse.audiostreaming.mixer.ImmerseMixer;
import com.programyourhome.immerse.domain.Scenario;

/**
 * The warmup scenario generator can generate a list of scenarios to be used as JVM warmup activity.
 * The goal of the generated scenarios is to cover the entire code base as well as possible,
 * so all code paths have been hit several times before real scenarios are played.
 *
 * Technical note: this is needed, because without it, the first scenario to play will have hickups
 * because of the way the JVM works. Code will run very slow the first time it gets 'hit' and
 * speed up when it is used more often. Read online about 'JVM warmup' for more detailed information.
 */
public interface WarmupScenarioGenerator {

    /**
     * Generate a map of warmup scenarios for the given mixer.
     * Key is the scenario, value is the scenario running time (in case of smooth play).
     */
    public Map<Scenario, Long> generateWarmupScenarios(ImmerseMixer mixer);

}
