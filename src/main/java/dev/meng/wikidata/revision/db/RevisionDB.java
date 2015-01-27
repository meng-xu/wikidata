/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.revision.db;

import dev.meng.wikidata.lib.db.DBException;
import dev.meng.wikidata.lib.db.SQLDB;

/**
 *
 * @author xumeng
 */
public class RevisionDB extends SQLDB{

    public PageTable PAGE;
    public RevisionTable REVISION;
    
    public RevisionDB(String filename) throws DBException {
        super(filename);
        
        PAGE = new PageTable(this.connection);
        this.tables.add(PAGE);

        REVISION = new RevisionTable(this.connection);
        this.tables.add(REVISION);           
    }    
}
