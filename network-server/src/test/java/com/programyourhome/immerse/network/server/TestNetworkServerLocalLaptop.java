package com.programyourhome.immerse.network.server;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.writers.ConsoleWriter;

public class TestNetworkServerLocalLaptop {

    public static void main(String[] args) throws Exception {
        // Configure logging
        Configurator.defaultConfig()
                .formatPattern("{date:yyyy-MM-dd HH:mm:ss} [{thread}] {class_name}.{method}() - {level}: {message}")
                .writer(new ConsoleWriter())
                .level(Level.DEBUG)
                .activate();

        new ImmerseServer(51515).start();
    }

}
