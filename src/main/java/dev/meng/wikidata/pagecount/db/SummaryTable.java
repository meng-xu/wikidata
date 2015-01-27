/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.pagecount.db;

import dev.meng.wikidata.lib.db.SQLTable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author xumeng
 */
public class SummaryTable extends SQLTable{

    public SummaryTable(Connection database) {
        super(Summary.class, database);
    }
    
    public List<Map<Summary, Object>> retrieveByLang(String lang) throws SQLException{
        Map<Summary, Object> criteria = new HashMap<>();
        criteria.put(Summary.LANG, lang);
        
        return this.select(new Summary[]{Summary.TIMESTAMP, Summary.FREQUENCY, Summary.SIZE}, criteria);
    }
    
    public List<Map<Summary, Object>> retrieveByTimestamp(int timestamp) throws SQLException{
        Map<Summary, Object> criteria = new HashMap<>();
        criteria.put(Summary.TIMESTAMP, timestamp);
        
        return this.select(new Summary[]{Summary.LANG, Summary.FREQUENCY, Summary.SIZE}, criteria);
    }  
    
    public List<Map<Summary, Object>> retrieveByLangAndTimestamp(String lang, int timestamp) throws SQLException{
        Map<Summary, Object> criteria = new HashMap<>();
        criteria.put(Summary.LANG, lang);
        criteria.put(Summary.TIMESTAMP, timestamp);
        
        return this.select(new Summary[]{Summary.FREQUENCY, Summary.SIZE}, criteria);
    }       
}
