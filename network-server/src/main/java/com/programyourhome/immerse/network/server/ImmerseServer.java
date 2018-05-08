package com.programyourhome.immerse.network.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.pmw.tinylog.Logger;

import com.programyourhome.immerse.audiostreaming.mixer.ImmerseMixer;
import com.programyourhome.immerse.network.server.action.ActionResult;
import com.programyourhome.immerse.network.server.action.ServerAction;

/**
 * A server that allows clients to control a mixer over the network.
 * If a client connects to the server and does nothing or does not follow the right protocol, the server might become unresponsive.
 * Therefore, it is recommended to always use the ImmerseClient for communication with the server.
 * See the Javadoc on the ImmerseClient class for more information on the workings of the protocol and lifecycle.
 */
public class ImmerseServer {

    private ServerSocket serverSocket;
    private ImmerseMixer mixer;
    private final int port;
    private boolean shouldStop;

    /**
     * Create a new server that should listen on the specified port.
     * To actually start listening, call the 'start' method.
     */
    public ImmerseServer(int port) {
        this.port = port;
        this.shouldStop = false;
    }

    /**
     * Get the mixer for this server (can be null).
     */
    public ImmerseMixer getMixer() {
        return this.mixer;
    }

    /**
     * Set the mixer for this server. Possible scenario's:
     * - mixer was null and is now set to an object (created)
     * - mixer was an object and is now set to null (removed)
     * Overriding a mixer is not possible.
     */
    public void setMixer(ImmerseMixer mixer) {
        if (this.hasMixer() && mixer != null) {
            throw new IllegalStateException("Mixer was already set, overriding is not possible");
        }
        this.mixer = mixer;
    }

    /**
     * Whether or not the server has a mixer.
     */
    public boolean hasMixer() {
        return this.mixer != null;
    }

    /**
     * Start listening on the configured port for connecting clients.
     * This method will block waiting for clients and therefore should be called in a new Thread.
     * Individual clients will not be handled in a separate thread, since they are short lived and
     * this prevents concurrency issues.
     * An accepted risk of this approach is that one 'rogue client' can stall the whole server.
     */
    public void start() {
        try (ServerSocket serverSocketResource = new ServerSocket(this.port)) {
            this.serverSocket = serverSocketResource;
            Logger.info("Server started and accepting connections.");
            while (!this.shouldStop) {
                try (Socket clientSocket = this.serverSocket.accept()) {
                    this.handleClient(clientSocket);
                } catch (Exception e) {
                    Logger.error(e, "Exception occured handling client");
                }
            }
        } catch (Exception e) {
            // If shouldStop is true, a SocketException is expected.
            if (!this.shouldStop) {
                Logger.error(e, "Exception occured in server");
            }
        }
        Logger.info("Server stopped.");
    }

    public void stop() {
        this.shouldStop = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            Logger.error(e, "Exception while closing server socket");
        }
    }

    /**
     * Handle the client connection.
     * Receive the action to take and forward processing to the corresponding action object.
     */
    private void handleClient(Socket clientSocket) throws IOException {
        ObjectInput objectInput = new ObjectInputStream(clientSocket.getInputStream());
        ActionResult<?> actionResult;
        try {
            // Read action to take.
            ServerAction action = (ServerAction) objectInput.readObject();
            Logger.info("Client connected, requested action: " + action);

            // Forward processing to the corresponding action implementation
            Object result = action.getAction().perform(this, objectInput);
            actionResult = ActionResult.success(result);
        } catch (Exception e) {
            Logger.error(e, "Exception occured during client handling");
            actionResult = ActionResult.error(e);
        }
        // Write the result back to the client.
        ObjectOutput objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
        objectOutput.writeObject(actionResult);

        objectInput.close();
        objectOutput.close();
    }

}
