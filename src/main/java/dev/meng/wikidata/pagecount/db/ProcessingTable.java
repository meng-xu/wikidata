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
public class ProcessingTable extends SQLTable{

    public ProcessingTable(Connection database) {
        super(Processing.class, database);
    }
        
    public boolean isProcessed(long timestamp) throws SQLException{
        Map<Processing, Object> criteria = new HashMap<>();
        criteria.put(Processing.TIMESTAMP, timestamp);
        
        List<Map<Processing, Object>> records = this.select(new Processing[]{Processing.PROCESSING}, criteria);
        return !records.isEmpty();
    }
}
