/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.config;

import dev.meng.wikidata.lib.config.ConfigException;
import dev.meng.wikidata.lib.config.PropertiesBasedConfig;

/**
 *
 * @author xumeng
 */
public class PagecountConfig extends PropertiesBasedConfig{

    public String TIMESTAMP_FORMAT;
    public String FILENAME_FORMAT;
    public long HTTP_WAIT_TIME;
    public String DOWNLOAD_SOURCE;
    public int FREQUENCY_THRESHOLD;
    public String DATA_REPOSITORY;
    public String DB_LOCATION;
    public String LOG_DIRECTORY;
    
    public PagecountConfig(String filename) throws ConfigException {
        super(filename);
    }
    
}
