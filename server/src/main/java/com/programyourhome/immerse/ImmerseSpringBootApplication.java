package com.programyourhome.immerse;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.programyourhome.immerse.server.ImmerseServer;

@SpringBootApplication
public class ImmerseSpringBootApplication {

    public static void main(final String[] args) {
        // TODO: document why and that this only works when starting in a new JVM.
        // TODO: do we actually need this?
        System.setProperty("java.net.preferIPv4Stack", "true");
        ImmerseServer.startServer(ImmerseSpringBootApplication.class);
    }

}