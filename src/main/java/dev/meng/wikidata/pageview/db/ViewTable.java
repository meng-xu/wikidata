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
import java.util.List;
import java.util.Map;

/**
 *
 * @author xumeng
 */
public class ViewTable extends SQLTable{

    public ViewTable(Connection database) {
        super(View.class, database);
    }
    
    public List<Map<View, Object>> retrieveByPageId(String pageId) throws SQLException{
        Map<View, Object> criteria = new HashMap<>();
        criteria.put(View.PAGE_ID, pageId);
        
        return this.select(new View[]{View.TIMESTAMP, View.FREQUENCY, View.SIZE}, criteria);
    }
}
