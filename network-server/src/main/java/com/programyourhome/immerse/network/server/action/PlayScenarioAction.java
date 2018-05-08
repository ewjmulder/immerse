package com.programyourhome.immerse.network.server.action;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.UUID;

import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Play a scenario on the mixer.
 */
public class PlayScenarioAction extends Action<UUID> {

    @Override
    public UUID perform(ImmerseServer server, ObjectInput objectInput) throws ClassNotFoundException, IOException {
        Scenario scenario = this.read(objectInput, Scenario.class);

        if (!server.hasMixer()) {
            throw new IllegalStateException("Server does not have a mixer, playing a scenario is not possible");
        }
        return server.getMixer().playScenario(scenario);
    }

}
