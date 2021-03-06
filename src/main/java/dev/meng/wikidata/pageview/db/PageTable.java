/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.pageview.db;

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
    
    public Map<String, Integer> retrievePageIdToIdMapByUnique(String lang, List<Map<Page, Object>> records) throws SQLException{
        Map<String, Integer> map = new HashMap<>();
        
        for(Map<Page, Object> record : records){
            Map<Page, Object> criteria = new HashMap<>();
            criteria.put(Page.LANG, record.get(Page.LANG));
            criteria.put(Page.PAGE_ID, record.get(Page.PAGE_ID));
            
            List<Map<Page, Object>> result = this.select(new Page[]{Page.PAGE_ID, Page.ID}, criteria);
            
            if(result.size()==1){
                Map<Page, Object> updatedRecord = result.get(0);
                map.put((String) updatedRecord.get(Page.PAGE_ID), (Integer) updatedRecord.get(Page.ID));
            } else{
                throw new SQLException("Unable to retrieve by unique column with record: "+record.toString());
            }
        }

        return map;
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
        return this.select(new Page[]{Page.ID, Page.LANG, Page.TITLE}, criteria);
    }
    
    public List<String> retrieveTitlesByLang(String lang) throws SQLException{
        List<String> result = new LinkedList<>();
        
        Map<Page, Object> criteria = new HashMap<>();
        criteria.put(Page.LANG, lang);
        
        List<Map<Page, Object>> records = this.select(new Page[]{Page.TITLE}, criteria);
        for(Map<Page, Object> record : records){
            result.add((String) record.get(Page.TITLE));
        }
        
        return result;
    }
    
    public String retrieveIdByLangAndTitle(String lang, String title) throws SQLException{
        Map<Page, Object> criteria = new HashMap<>();
        criteria.put(Page.LANG, lang);
        criteria.put(Page.TITLE, title);
        
        List<Map<Page, Object>> result = this.select(new Page[]{Page.ID}, criteria);
        if(result.size()!=1){
            return null;
        } else{
            return (String) result.get(0).get(Page.ID);
        }
    }
    
    public Map<String, Map<Page, Object>> retrieveUniqueToRecordMap (List<Map<Page, Object>> records) throws SQLException{
        Map<String, Map<Page, Object>> map = new HashMap<>();
        
        for(Map<Page, Object> record : records){
            Map<Page, Object> criteria = new HashMap<>();
            criteria.put(Page.LANG, record.get(Page.LANG));
            criteria.put(Page.TITLE, record.get(Page.TITLE));
            
            List<Map<Page, Object>> result = this.select(new Page[]{Page.LANG, Page.TITLE, Page.ID}, criteria);
            
            if(result.size()==1){
                Map<Page, Object> updatedRecord = result.get(0);
                map.put(updatedRecord.get(Page.LANG) + "/" + updatedRecord.get(Page.TITLE), updatedRecord);
            } else{
                throw new SQLException("Unable to retrieve by unique column with record: "+record.toString());
            }
        }
        
        return map;
    }    
}
