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
import java.util.List;
import java.util.Map;

/**
 *
 * @author xumeng
 */
public class FileTable extends SQLTable{

    public FileTable(Connection database) {
        super(File.class, database);
    }
    
    public List<Map<File, Object>> retrieveAll() throws SQLException{
        Map<File, Object> criteria = new HashMap<>();
        return this.select(new File[]{File.ID, File.LANG, File.TITLE, File.SIZE}, criteria);
    }    
}
