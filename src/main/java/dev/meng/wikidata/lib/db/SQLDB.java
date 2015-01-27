/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.lib.db;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author xumeng
 */
public class SQLDB {
    
    protected String name;
    protected Connection connection;
    protected List<SQLTable> tables;
    
    public SQLDB(String jdbc) throws DBException {
        try {
            tables = new LinkedList<>();
            
            connection = DriverManager.getConnection(jdbc);
            connection.setAutoCommit(false);
            
            name = Paths.get(connection.getMetaData().getURL()).getFileName().toString();
        } catch (SQLException ex) {
            throw new DBException(ex);
        }

    }

    public String name(){
        return name;
    }
    
    public void init() throws SQLException{
        for(SQLTable table : tables){
            table.init();
        }
    }

    public List<Map<String, Object>> select(String sql, Object... objs) throws SQLException{
        List<Map<String, Object>> result = new LinkedList<>();
        
        PreparedStatement statement = connection.prepareStatement(sql);
        
        for (int i = 1; i <= objs.length; i++) {
            statement.setObject(i, objs[i-1]);
        }

        ResultSet results = statement.executeQuery();
        ResultSetMetaData metadata = results.getMetaData();
        int columnCount = metadata.getColumnCount();

        while (results.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metadata.getColumnName(i), results.getObject(i));
            }
            result.add(row);
        }

        return result;
    }    
}
