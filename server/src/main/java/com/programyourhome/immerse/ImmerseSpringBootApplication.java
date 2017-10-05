package com.programyourhome.immerse;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.programyourhome.immerse.server.ImmerseServer;

@SpringBootApplication
public class ImmerseSpringBootApplication {

    public static void startApplication() {
        ImmerseServer.startServer(ImmerseSpringBootApplication.class);
    }

}