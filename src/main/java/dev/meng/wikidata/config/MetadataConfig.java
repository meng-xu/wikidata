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
public class MetadataConfig extends PropertiesBasedConfig{

    public String API_ENDPOINT;
    public int REQUEST_LENGTH_MAX;
    public String TIMESTAMP_FORMAT;
    public String DEFAULT_ENCODING;
    public String DB_LOCATION;
    public String LOG_DIRECTORY;
    
    public MetadataConfig(String filename) throws ConfigException {
        super(filename);
    }
    
}
