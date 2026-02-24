package com.gmail.sync667.gougouserver.log;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.gmail.sync667.gougouserver.GouGouServer;

public class ConsoleLogger {

    private final Logger logger;
    private final Console console;
    private final File log;
    private final Path logPath;
    final static Charset ENCODING = StandardCharsets.UTF_8;

    public ConsoleLogger(Console console) {
        this.console = console;

        logger = Logger.getLogger("GouGouServer");

        logPath = Paths.get(GouGouServer.class.getProtectionDomain().getCodeSource().getLocation().getPath()
                + File.separator + "log.txt");

        log = logPath.toFile();
    }

    public void logFile(String message) {

        if (!log.exists()) {
            try {
                log.createNewFile();
            } catch (IOException e) {
                infoC("Failed to create log file!");
                return;
            }
        }
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(log, true)))) {
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void info(String message) {
        logger.info(message.trim() + "\n");
        logFile(message.trim());
    }

    public void warning(String message) {
        logger.warning(message.trim() + "\n");
        logFile(message.trim());
    }

    public void severe(String message) {
        logger.severe(message.trim() + "\n");
        logFile(message.trim());
    }

    public void infoC(String message) {
        console.printf(message.trim() + "\n");
        logFile(message.trim());
    }

}
