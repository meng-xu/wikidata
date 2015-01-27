/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata;

import dev.meng.wikidata.config.MetadataConfig;
import dev.meng.wikidata.config.PagecountConfig;
import dev.meng.wikidata.config.PageviewConfig;
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
    public static final MetadataConfig METADATA = loadMetadataConfig();
    
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
    
    private static MetadataConfig loadMetadataConfig() {
        try {
            MetadataConfig config = new MetadataConfig("config/metadata.properties");
            Logger.log(Configure.class, LogLevel.INFO, "metadata config loaded");
            return config;
        } catch (ConfigException ex) {
            Logger.log(Configure.class, LogLevel.ERROR, "unable to load metadata config", ex);
            return null;
        }
    }    
}