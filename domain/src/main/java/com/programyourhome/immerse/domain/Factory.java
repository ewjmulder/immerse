package com.programyourhome.immerse.domain;

import java.io.Serializable;

/**
 * A Factory can be seen as a Supplier that implements Serializable.
 * It can create new instances of a certain type that also implements Serializable.
 */
@FunctionalInterface
public interface Factory<T extends Serializable> extends Serializable {

    /**
     * Create a new instance of <T> that is functionally identical, but not object identical to others created by this method.
     */
    public T create();

}
