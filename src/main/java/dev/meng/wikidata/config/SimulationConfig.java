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
public class SimulationConfig extends PropertiesBasedConfig{

    public double USER_PERCENTAGE;
    public String ACCESS_DIST;
    public double ACCESS_DIST_SD_TO_MEAN_RATIO;
    public String ACCESS_DURATION_DIST;
    public long ACCESS_DURATION_MEAN;
    public double ACCESS_DURATION_SD_TO_MEAN_RATIO;
    public String DB_LOCATION;
    public String LOG_DIRECTORY;
    
    public SimulationConfig(String filename) throws ConfigException {
        super(filename);
    }
    
}
