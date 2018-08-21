package com.programyourhome.immerse.network.client;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import com.programyourhome.immerse.audiostreaming.mixer.ImmerseSettings;
import com.programyourhome.immerse.domain.Scenario;
import com.programyourhome.immerse.network.server.action.ActionResult;
import com.programyourhome.immerse.network.server.action.ServerAction;

/**
 * An Immerse client can send instructions to a remote Immerse server.
 * It exposes a Java API for applications to use and control an Immerse Mixer running somewhere on the network.
 * Internally it uses a JVM serialization based protocol to communicate between client and server.
 * All method calls will block on network IO until the server has responded or an IO exception has occurred.
 * A new connection will be created for every action and closed afterwards.
 *
 * The methods should be called in this order:
 * 1. createMixer
 * 2. startMixer
 * 3. Zero or more times: playScenario
 * 4. stopMixer
 *
 * After stopMixer, you can start over with createMixer.
 * The methods checkConnection and hasMixer can be called at any time.
 * Not complying with this order will result in error results.
 */
public class ImmerseClient {

    private final String host;
    private final int port;

    /**
     * Create a client for the server running on 'host:port'.
     * The constructor will also perform a connection check to the specified server.
     */
    public ImmerseClient(String host, int port) {
        this.host = host;
        this.port = port;
        ActionResult<Void> checkConnection = this.checkConnection();
        if (!checkConnection.isSuccess()) {
            throw new IllegalStateException("Connection check failed: " + checkConnection.getErrorMessage());
        }
    }

    /**
     * Check the connection to the server.
     */
    public ActionResult<Void> checkConnection() {
        return this.callServer(ServerAction.CHECK_CONNECTION);
    }

    /**
     * Whether the server already has created a mixer or not.
     */
    public ActionResult<Boolean> hasMixer() {
        return this.callServer(ServerAction.HAS_MIXER);
    }

    /**
     * Create a new mixer.
     */
    public ActionResult<Void> createMixer(ImmerseSettings settings) {
        return this.callServer(ServerAction.CREATE_MIXER, settings);
    }

    /**
     * Start the mixer.
     */
    public ActionResult<Void> startMixer() {
        return this.callServer(ServerAction.START_MIXER);
    }

    /**
     * Play a scenario on the mixer.
     */
    public ActionResult<UUID> playScenario(Scenario scenario) {
        return this.callServer(ServerAction.PLAY_SCENARIO, scenario);
    }

    /**
     * Wait for completion of a playback on the mixer.
     */
    public ActionResult<Void> waitForPlayback(UUID playbackId) {
        return this.callServer(ServerAction.WAIT_FOR_PLAYBACK, playbackId);
    }

    /**
     * Stop playback of a scenario on the mixer.
     */
    public ActionResult<Void> stopPlayback(UUID playbackId) {
        return this.callServer(ServerAction.STOP_PLAYBACK, playbackId);
    }

    /**
     * Stop the mixer.
     */
    public ActionResult<Void> stopMixer() {
        return this.callServer(ServerAction.STOP_MIXER);
    }

    /**
     * Call the server with the specified action and parameters.
     * This can be seen as some form of RMI (Remote Method Invocation).
     * Creates and closes a new TCP connection every time.
     */
    private <T> ActionResult<T> callServer(ServerAction action, Object... parameters) {
        try (Socket socket = new Socket(this.host, this.port)) {
            ObjectOutput objectOutput = new ObjectOutputStream(socket.getOutputStream());
            // Write the action to take to the server.
            objectOutput.writeObject(action);
            // Write the action parameters to the server.
            for (Object parameter : parameters) {
                objectOutput.writeObject(parameter);
            }

            // Receive result from server.
            ObjectInput objectInput = new ObjectInputStream(socket.getInputStream());
            @SuppressWarnings("unchecked")
            ActionResult<T> actionResult = (ActionResult<T>) objectInput.readObject();

            objectInput.close();
            objectOutput.close();

            return actionResult;
        } catch (Exception e) {
            return ActionResult.error(e);
        }
    }

}
