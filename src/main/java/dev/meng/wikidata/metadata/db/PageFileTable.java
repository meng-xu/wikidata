/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.metadata.db;

import dev.meng.wikidata.lib.db.SQLTable;
import java.sql.Connection;

/**
 *
 * @author xumeng
 */
public class PageFileTable extends SQLTable{

    public PageFileTable(Connection database) {
        super(PageFile.class, database);
    }
}
