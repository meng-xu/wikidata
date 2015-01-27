/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.fileusage.db;

import dev.meng.wikidata.lib.db.DBException;
import dev.meng.wikidata.lib.db.SQLDB;

/**
 *
 * @author xumeng
 */
public class FileusageDB extends SQLDB{

    public PageTable PAGE;
    public FileTable FILE;
    public PageFileTable PAGE_FILE;
    
    public FileusageDB(String filename) throws DBException {
        super(filename);
        
        PAGE = new PageTable(this.connection);
        this.tables.add(PAGE);
        
        FILE = new FileTable(this.connection);
        this.tables.add(FILE);     
        
        PAGE_FILE = new PageFileTable(this.connection);
        this.tables.add(PAGE_FILE);           
    }    
}
