package com.programyourhome.immerse.network.server.action;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.Collection;

import com.programyourhome.immerse.network.server.ImmerseServer;

/**
 * Base class for all action implementations.
 */
public abstract class Action<T> {

    // No instance possible of type Void, but using a constant looks better than 'return null'.
    protected static final Void VOID_RETURN_VALUE = null;

    /**
     * Read an object of a certain type from the input.
     */
    @SuppressWarnings("unchecked")
    protected <R> R read(ObjectInput objectInput, Class<R> type) throws ClassNotFoundException, IOException {
        return (R) objectInput.readObject();
    }

    /**
     * Read a collection of objects of a certain type from the input.
     */
    @SuppressWarnings("unchecked")
    protected <R> Collection<R> readCollection(ObjectInput objectInput, Class<R> elementType) throws ClassNotFoundException, IOException {
        return (Collection<R>) objectInput.readObject();
    }

    /**
     * Perform the action and return a value of the configured type.
     */
    public abstract T perform(ImmerseServer server, ObjectInput objectInput) throws Exception;

}
