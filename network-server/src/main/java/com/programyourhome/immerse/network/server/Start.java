package com.programyourhome.immerse.network.server;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.writers.ConsoleWriter;

/**
 * The Start class contains a main method to start the Immerse Network Server.
 */
public class Start {

    public static final int DEFAULT_PORT = 51515;

    public static void main(String[] args) {
        // TODO: print usage if --help
        // TODO: make dynamic
        Configurator.defaultConfig()
                .formatPattern("{date:yyyy-MM-dd HH:mm:ss} [{thread}] {class_name}.{method}() - {level}: {message}")
                .writer(new ConsoleWriter())
                .level(Level.DEBUG)
                .activate();

        int port = DEFAULT_PORT;
        if (args.length > 0) {
            // TODO: handle error
            port = Integer.parseInt(args[0]);
        }
        new ImmerseServer(port).start();
    }

}
