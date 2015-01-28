/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.simulation;

import dev.meng.wikidata.DB;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author meng
 */
@Loggable(output=LogOutput.FILE)
public class Simulator {
    
    private Generator generator = new Generator();
    
    public void simulate(String lang, GregorianCalendar start, GregorianCalendar end){
        List<AccessRecord> accessRecords = new LinkedList<>();
        GregorianCalendar current = start;
        while(current.getTimeInMillis()<=end.getTimeInMillis()){
            try {
                List<Map<String, Object>> records = DB.PAGEVIEW.getAllRecordsByLangAndTimestamp(lang, current);
                for(Map<String, Object> record : records){
                    accessRecords.addAll(generator.generateSparsePattern(lang, (String)record.get("PAGE_ID"), ((Number)record.get("FREQUENCY")).longValue(), current.getTimeInMillis(), 3600000L));
                }
            } catch (SQLException ex) {
                Logger.log(this.getClass(), LogLevel.ERROR, ex);
            }
            current.add(GregorianCalendar.HOUR_OF_DAY, 1);
        }    
System.out.println(accessRecords);
    }
}
