package com.programyourhome.immerse.domain.volume;

import java.io.Serializable;

/**
 * A volume that changes over time.
 */
public interface DynamicVolume extends Serializable {

    /**
     * Get the volume [0,1] on a certain moment in time.
     * For robustness, implementations are advised to accept any value, both negative and positive.
     */
    public double getVolume(long millisSinceStart);

}
