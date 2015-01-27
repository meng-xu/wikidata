/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.lib.log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author meng
 */
public class Logger {
    
    private static final String LOG_DIR = System.getProperty("app.log.dir")==null ? "log/" : System.getProperty("app.log.dir");
    private static final FileHandler HANDLER = createGeneralHandler();
    
    private static Map<Class, Map<LogLevel, java.util.logging.Logger>> loggers = new HashMap<>();

    private static void register(Class cls) {
        if(cls.isAnnotationPresent(Loggable.class)){
            Map<LogLevel, java.util.logging.Logger> loggerMap = new HashMap<>();
            Loggable annotation = (Loggable) cls.getAnnotation(Loggable.class);
            switch(annotation.output()){
                case CONSOLE:
                    for (LogLevel level : LogLevel.values()) {
                        java.util.logging.Logger logger = createConsoleLogger(cls, level);
                        loggerMap.put(level, logger);
                    }
                    break;
                case FILE:
                    for (LogLevel level : LogLevel.values()) {                      
                        java.util.logging.Logger logger = createFileLogger(cls, level, Paths.get(LOG_DIR));
                        loggerMap.put(level, logger);
                    }
            }

            loggers.put(cls, loggerMap);
        }
    }

    public static void log(Class cls, LogLevel level, String message) {
        if (loggers.get(cls) == null) {
            register(cls);
        }
        loggers.get(cls).get(level).log(Level.SEVERE, message);
    }

    public static void log(Class cls, LogLevel level, Throwable exception) {
        if (loggers.get(cls) == null) {
            register(cls);
        }
        loggers.get(cls).get(level).log(Level.SEVERE, null, exception);
    }

    public static void log(Class cls, LogLevel level, String message, Throwable exception) {
        if (loggers.get(cls) == null) {
            register(cls);
        }
        loggers.get(cls).get(level).log(Level.SEVERE, message, exception);
    }
    
    private static java.util.logging.Logger createConsoleLogger(Class cls, LogLevel level) {
        String loggerName = cls.getName() + "-" + level.name().toLowerCase();
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(loggerName);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LogFormatter());
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        logger.addHandler(HANDLER);
        return logger;
    }
    
    private static java.util.logging.Logger createFileLogger(Class cls, LogLevel level, Path dir) {
        String loggerName = cls.getName() + "-" + level.name().toLowerCase();
        String filename = cls.getSimpleName()+ "-" + level.name().toLowerCase();
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(loggerName);
        logger.setUseParentHandlers(false);
        try {
            if(!Files.isDirectory(dir)){
                Files.createDirectories(dir);
            }
            FileHandler handler = new FileHandler(dir.resolve(filename + ".log").toString(), true);
            handler.setFormatter(new LogFormatter());
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
            logger.addHandler(HANDLER);
        } catch (IOException | SecurityException ex) {
            ex.printStackTrace();
        }
        return logger;
    }
    
    private static FileHandler createGeneralHandler() {
        try {
            FileHandler handler = new FileHandler(Paths.get(LOG_DIR, "all-in-one.log").toString(), true);
            handler.setFormatter(new SimpleFormatter());
            return handler;
        } catch (IOException | SecurityException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
