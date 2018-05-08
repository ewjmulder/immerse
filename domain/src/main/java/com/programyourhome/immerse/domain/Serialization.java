package com.programyourhome.immerse.domain;

/**
 * Interface to hold the current version value for all serialVersionUID fields in the application.
 * The serialization version represents the version of the application.
 * Since leading 0's are not accepted, we need a prefix. And what is a cooler prefix then '42'? ;-)
 */
public interface Serialization {

    /**
     * Serialization version for all Serializable classes in the application.
     */
    public static final long VERSION = 42_080L;

}
