/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.analyze;

import dev.meng.wikidata.DB;
import dev.meng.wikidata.fileusage.Fileusage;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;
import dev.meng.wikidata.revision.RevisionHistory;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author meng
 */
@Loggable(output=LogOutput.FILE)
public class Analyzer {
    
    public void analyzeFileusage(String lang, GregorianCalendar start, GregorianCalendar end, int limit){
        try {
            List<Map<String, Object>> records = DB.PAGEVIEW.getTopViewsByLangAndTimestamp(lang, start, end, limit);
            
            Set<dev.meng.wikidata.fileusage.PageInfo> pages = new HashSet<>();
            for(Map<String, Object> record : records){
                dev.meng.wikidata.fileusage.PageInfo page = new dev.meng.wikidata.fileusage.PageInfo();
                page.setLang((String) record.get("LANG"));
                page.setPageId((String) record.get("PAGE_ID"));
                pages.add(page);
            }
            
            Fileusage fileusage = new Fileusage();
            fileusage.queryFileusage(pages);
            
        } catch (SQLException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        }
    }
    public void analyzeRevision(String lang, GregorianCalendar start, GregorianCalendar end, int limit, GregorianCalendar revStart, GregorianCalendar revEnd){
        try {
            List<Map<String, Object>> records = DB.PAGEVIEW.getTopViewsByLangAndTimestamp(lang, start, end, limit);
            
            Set<dev.meng.wikidata.revision.PageInfo> pages = new HashSet<>();
            for(Map<String, Object> record : records){
                dev.meng.wikidata.revision.PageInfo page = new dev.meng.wikidata.revision.PageInfo();
                page.setLang((String) record.get("LANG"));
                page.setPageId((String) record.get("PAGE_ID"));
                pages.add(page);
            }
            
            RevisionHistory revision = new RevisionHistory();
            revision.queryRevisions(pages, revStart, revEnd);
            
        } catch (SQLException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        }
    }    
    
    public long analyzeTopFrequency(String lang, GregorianCalendar start, GregorianCalendar end, int limit){
        try {
            long result = 0L;
            List<Map<String, Object>> records = DB.PAGEVIEW.getTopViewsByLangAndTimestamp(lang, start, end, limit);

            for(Map<String, Object> record : records){
                result = result + ((Number)record.get("FREQUENCY_SUM")).longValue();
            }
            return result;
            
        } catch (SQLException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
            return -1L;
        }
    }
    
    public long analyzeTotalFrequency(String lang, GregorianCalendar start, GregorianCalendar end){
        try {
            return DB.PAGEVIEW.getTotalFrequency(lang, start, end);
        } catch (SQLException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
            return -1L;
        }
    }
}
