/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.simulation;

import dev.meng.wikidata.Configure;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author meng
 */
public class Generator {
    
    private Random rand = new Random();
    private long sequence = 1;
    
    public List<AccessRecord> generateSparsePattern(String lang, String pageId, long frequency, long offset, long interval){
        List<AccessRecord> result = new LinkedList<>();
        
        long simFrequency = Math.round(frequency * Configure.SIMULATION.USER_PERCENTAGE);
        
        long firstAccessMean = Math.round(interval / (2.0 * simFrequency));
        long accessSD = Math.round(firstAccessMean * Configure.SIMULATION.ACCESS_DIST_SD_TO_MEAN_RATIO);
        
        long durationSD = Math.round(Configure.SIMULATION.ACCESS_DURATION_MEAN * Configure.SIMULATION.ACCESS_DURATION_SD_TO_MEAN_RATIO);
        
        for(long i=0;i<simFrequency;i++){
            long accessTime = offset + i * firstAccessMean + Math.round(accessSD * rand.nextGaussian());
            long duration = Configure.SIMULATION.ACCESS_DURATION_MEAN + Math.round(durationSD * rand.nextGaussian());
            AccessRecord record = new AccessRecord(sequence, lang, pageId, accessTime, accessTime + duration);
            sequence++;
            result.add(record);
        }
        
        return result;
    }
    
    public List<AccessRecord> generateRandomPattern(String lang, String pageId, long frequency, long offset, long interval){
        List<AccessRecord> result = new LinkedList<>();
        
        long simFrequency = Math.round(frequency * Configure.SIMULATION.USER_PERCENTAGE);

        long durationSD = Math.round(Configure.SIMULATION.ACCESS_DURATION_MEAN * Configure.SIMULATION.ACCESS_DURATION_SD_TO_MEAN_RATIO);
        
        for(long i=0;i<simFrequency;i++){
            long accessTime = offset + rand.nextInt((int) (interval + 1));
            long duration = Configure.SIMULATION.ACCESS_DURATION_MEAN + Math.round(durationSD * rand.nextGaussian());
            AccessRecord record = new AccessRecord(sequence, lang, pageId, accessTime, accessTime + duration);
            sequence++;
            result.add(record);
        }
        
        return result;
    }    
}
