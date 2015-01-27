/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.metadata.db;

import dev.meng.wikidata.lib.db.SQLTable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author xumeng
 */
public class PageTable extends SQLTable{

    public PageTable(Connection database) {
        super(Page.class, database);
    }
    
    public Map<String, Integer> retrievePageIdToIdMapByLang(String lang) throws SQLException{
        Map<String, Integer> result = new HashMap<>();
        
        Map<Page, Object> criteria = new HashMap<>();
        criteria.put(Page.LANG, lang);
        
        List<Map<Page, Object>> records = this.select(new Page[]{Page.PAGE_ID, Page.ID}, criteria);
        for(Map<Page, Object> record : records){
            result.put((String) record.get(Page.PAGE_ID), (Integer) record.get(Page.ID));
        }
        
        return result;
    }
    
    public List<String> retrieveAllLangs() throws SQLException{
        List<String> result = new LinkedList<>();
        
        Map<Page, Object> criteria = new HashMap<>();
        
        List<Map<Page, Object>> records = this.selectDistinct(new Page[]{Page.LANG}, criteria);
        for(Map<Page, Object> record : records){
            result.add((String) record.get(Page.LANG));
        }
        
        return result;
    }
    
    public List<Map<Page, Object>> retrieveAll() throws SQLException{
        Map<Page, Object> criteria = new HashMap<>();
        return this.select(new Page[]{Page.ID, Page.LANG, Page.PAGE_ID}, criteria);
    }
}
