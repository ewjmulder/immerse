package com.programyourhome.immerse.domain.audio.resource;

import java.io.InputStream;
import java.io.Serializable;

/**
 * An audio resource is an object that can provide an InputStream.
 */
public interface AudioResource extends Serializable {

    /**
     * Get the InputStream for this audio resource.
     * This method should always return the same object.
     */
    public InputStream getInputStream();

}