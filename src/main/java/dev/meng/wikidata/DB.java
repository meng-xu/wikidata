/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata;

import dev.meng.wikidata.lib.db.DBException;
import dev.meng.wikidata.lib.db.SQLDB;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;
import dev.meng.wikidata.pagecount.db.PagecountDB;
import dev.meng.wikidata.metadata.db.MetadataDB;
import dev.meng.wikidata.pageview.db.PageviewDB;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author xumeng
 */
@Loggable(output=LogOutput.FILE)
public class DB {
    
    private static Map<String, SQLDB> list = new HashMap();
    
    public static final PagecountDB PAGECOUNT = loadPagecountDB();
    public static final PageviewDB PAGEVIEW = loadPageviewDB();
    public static final MetadataDB METADATA = loadMetadataDB();
    
    public static PagecountDB loadPagecountDB(){
        try {
            PagecountDB db = new PagecountDB(Configure.PAGECOUNT.DB_LOCATION);
            list.put(db.name(), db);
            Logger.log(DB.class, LogLevel.INFO, Configure.PAGECOUNT.DB_LOCATION+" loaded");
            return db;
        } catch (DBException ex) {
            Logger.log(DB.class, LogLevel.ERROR, "unable to load "+Configure.PAGECOUNT.DB_LOCATION, ex);
            return null;
        }
    }
    
    public static PageviewDB loadPageviewDB(){
        try {
            PageviewDB db = new PageviewDB(Configure.PAGEVIEW.DB_LOCATION);
            list.put(db.name(), db);
            Logger.log(DB.class, LogLevel.INFO, Configure.PAGEVIEW.DB_LOCATION+" loaded");
            return db;
        } catch (DBException ex) {
            Logger.log(DB.class, LogLevel.ERROR, "unable to load "+Configure.PAGEVIEW.DB_LOCATION, ex);
            return null;
        }
    }
    
    public static MetadataDB loadMetadataDB(){
        try {
            MetadataDB db = new MetadataDB(Configure.METADATA.DB_LOCATION);
            list.put(db.name(), db);
            Logger.log(DB.class, LogLevel.INFO, Configure.METADATA.DB_LOCATION+" loaded");
            return db;
        } catch (DBException ex) {
            Logger.log(DB.class, LogLevel.ERROR, "unable to load "+Configure.METADATA.DB_LOCATION, ex);
            return null;
        }
    }
    
    public static SQLDB get(String name){
        return list.get(name);
    }
    
    public static List<String> list(){
        return new LinkedList<>(list.keySet());
    }
}
