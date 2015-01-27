/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.pagecount.db;

import dev.meng.wikidata.lib.db.DBException;
import dev.meng.wikidata.lib.db.SQLDB;

/**
 *
 * @author xumeng
 */
public class PagecountDB extends SQLDB{

    public PageTable PAGE;
    public ViewTable VIEW;
    public SummaryTable SUMMARY;
    public ProcessingTable PROCESSING;
    
    public PagecountDB(String filename) throws DBException {
        super(filename);
        
        PAGE = new PageTable(this.connection);
        this.tables.add(PAGE);
        
        VIEW = new ViewTable(this.connection);
        this.tables.add(VIEW);     
        
        SUMMARY = new SummaryTable(this.connection);
        this.tables.add(SUMMARY);   
        
        PROCESSING = new ProcessingTable(this.connection);
        this.tables.add(PROCESSING);
    }    
}
