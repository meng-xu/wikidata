/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata;

import dev.meng.wikidata.config.FileusageConfig;
import dev.meng.wikidata.config.PagecountConfig;
import dev.meng.wikidata.config.PageviewConfig;
import dev.meng.wikidata.config.RevisionConfig;
import dev.meng.wikidata.config.SimulationConfig;
import dev.meng.wikidata.lib.config.ConfigException;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;

/**
 *
 * @author xumeng
 */
@Loggable(output=LogOutput.FILE)
public class Configure {
    public static final PagecountConfig PAGECOUNT = loadPagecountConfig();
    public static final PageviewConfig PAGEVIEW = loadPageviewConfig();
    public static final FileusageConfig FILEUSAGE = loadFileusageConfig();
    public static final RevisionConfig REVISION = loadRevisionConfig();
    public static final SimulationConfig SIMULATION = loadSimulationConfig();
    
    private static PagecountConfig loadPagecountConfig() {
        try {
            PagecountConfig config = new PagecountConfig("config/pagecount.properties");
            Logger.log(Configure.class, LogLevel.INFO, "pagecount config loaded");
            return config;
        } catch (ConfigException ex) {
            Logger.log(Configure.class, LogLevel.ERROR, "unable to load pagecount config", ex);
            return null;
        }
    }
    
    private static PageviewConfig loadPageviewConfig() {
        try {
            PageviewConfig config = new PageviewConfig("config/pageview.properties");
            Logger.log(Configure.class, LogLevel.INFO, "pageview config loaded");
            return config;
        } catch (ConfigException ex) {
            Logger.log(Configure.class, LogLevel.ERROR, "unable to load pageview config", ex);
            return null;
        }
    }
    
    private static FileusageConfig loadFileusageConfig() {
        try {
            FileusageConfig config = new FileusageConfig("config/fileusage.properties");
            Logger.log(Configure.class, LogLevel.INFO, "fileusage config loaded");
            return config;
        } catch (ConfigException ex) {
            Logger.log(Configure.class, LogLevel.ERROR, "unable to load fileusage config", ex);
            return null;
        }
    } 
    
    private static RevisionConfig loadRevisionConfig() {
        try {
            RevisionConfig config = new RevisionConfig("config/revision.properties");
            Logger.log(Configure.class, LogLevel.INFO, "revision config loaded");
            return config;
        } catch (ConfigException ex) {
            Logger.log(Configure.class, LogLevel.ERROR, "unable to load revision config", ex);
            return null;
        }
    }   
    
    private static SimulationConfig loadSimulationConfig() {
        try {
            SimulationConfig config = new SimulationConfig("config/simulation.properties");
            Logger.log(Configure.class, LogLevel.INFO, "simulation config loaded");
            return config;
        } catch (ConfigException ex) {
            Logger.log(Configure.class, LogLevel.ERROR, "unable to load simulation config", ex);
            return null;
        }
    }     
}