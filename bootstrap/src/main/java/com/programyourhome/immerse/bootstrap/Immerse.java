package com.programyourhome.immerse.bootstrap;

import com.programyourhome.immerse.ImmerseSpringBootApplication;

/**
 * Entry point of the application: starts the server.
 */
public class Immerse {

    public static void main(final String[] args) {
        // TODO: document why and that this only works when starting in a new JVM.
        System.setProperty("java.net.preferIPv4Stack", "true");
        ImmerseSpringBootApplication.startApplication();
    }

}