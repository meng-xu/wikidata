/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/* 
TODO use jOOQ or querydsl to replace the ugly SQL generation part
*/
package dev.meng.wikidata.lib.db;

import com.google.common.base.Joiner;
import dev.meng.wikidata.util.string.StringUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 *
 * @author xumeng
 */
public class SQLTable<T extends SQLTableDefinition> {
    
    private Connection connection;
    private T[] fields;
    private String name;
    private Map<String, T> mapping;
    
    public SQLTable(Class<T> definition, Connection connection){
        this.connection = connection;
        
        fields = definition.getEnumConstants();
        
        name = fields[0].getClass().getSimpleName();
        
        mapping = new HashMap<>();
        for(T field : fields){
            mapping.put(field.toString(), field);
        }
    }

    public void init() throws SQLException {
        String sql = "DROP TABLE IF EXISTS " + name;

        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);

        String[] columns = new String[fields.length];
        Map<SQLColumnAttribute, List<String>> attributeMap = new HashMap<>();

        for (int i = 0; i < columns.length; i++) {
            columns[i] = fields[i].getName() + " " + fields[i].getType();
            for (SQLColumnAttribute attribute : fields[i].getAttributes()) {
                if (attribute.isGroup()) {
                    List<String> columnList = attributeMap.get(attribute);
                    if (columnList == null) {
                        columnList = new LinkedList<>();
                    }
                    columnList.add(fields[i].getName());
                    attributeMap.put(attribute, columnList);
                } else {
                    columns[i] = columns[i] + " " + attribute.getRepr();
                }
            }
        }

        List<String> attributeStrings = new LinkedList<>();
        for (SQLColumnAttribute attribute : attributeMap.keySet()) {
            attributeStrings.add(attribute.getRepr() + "(" + Joiner.on(",").join(attributeMap.get(attribute)) + ")");
        }

        sql = "CREATE TABLE " + name + " (" + Joiner.on(",").join(columns) + "," + Joiner.on(",").join(attributeStrings) + ")";
        statement.executeUpdate(sql);

        connection.commit();

    }
    
    public void insert(Map<T, Object> record) throws SQLException {

        String sql = "INSERT INTO " + name + " (" + Joiner.on(",").join(record.keySet()) + ") VALUES (" + StringUtils.repetition("?", ",", record.keySet().size()) + ")";

        PreparedStatement statement = connection.prepareStatement(sql);

        Collection<Object> objs = record.values();
        Iterator<Object> iterator = objs.iterator();

        for (int i = 1; i <= objs.size(); i++) {
            statement.setObject(i, iterator.next());
        }

        statement.executeUpdate();

        connection.commit();

    }

    public void insertOrIgnore(Map<T, Object> record) throws SQLException {

        String sql = "INSERT OR IGNORE INTO " + name + " (" + Joiner.on(",").join(record.keySet()) + ") VALUES (" + StringUtils.repetition("?", ",", record.keySet().size()) + ")";

        PreparedStatement statement = connection.prepareStatement(sql);

        Collection<Object> objs = record.values();
        Iterator<Object> iterator = objs.iterator();

        for (int i = 1; i <= objs.size(); i++) {
            statement.setObject(i, iterator.next());
        }

        statement.executeUpdate();

        connection.commit();

    }

    public void insertOrReplace(Map<T, Object> record) throws SQLException {

        String sql = "INSERT OR REPLACE INTO " + name + " (" + Joiner.on(",").join(record.keySet()) + ") VALUES (" + StringUtils.repetition("?", ",", record.keySet().size()) + ")";

        PreparedStatement statement = connection.prepareStatement(sql);

        Collection<Object> objs = record.values();
        Iterator<Object> iterator = objs.iterator();

        for (int i = 1; i <= objs.size(); i++) {
            statement.setObject(i, iterator.next());
        }

        statement.executeUpdate();

        connection.commit();

    }
    
    public void insertBatch(List<Map<T, Object>> records) throws SQLException {

        if (!records.isEmpty()) {

            Map<T, Object> sample = records.get(0);
            String sql = "INSERT INTO " + name + " (" + Joiner.on(",").join(sample.keySet()) + ") VALUES (" + StringUtils.repetition("?", ",", sample.keySet().size()) + ")";

            PreparedStatement statement = connection.prepareStatement(sql);

            for (Map<T, Object> record : records) {
                Collection<Object> objs = record.values();
                Iterator<Object> iterator = objs.iterator();

                for (int i = 1; i <= objs.size(); i++) {
                    statement.setObject(i, iterator.next());
                }

                statement.addBatch();
            }

            statement.executeBatch();

            connection.commit();

        }
    }
    
    public void insertOrIgnoreBatch(List<Map<T, Object>> records) throws SQLException {

        if (!records.isEmpty()) {

            Map<T, Object> sample = records.get(0);
            String sql = "INSERT OR IGNORE INTO " + name + " (" + Joiner.on(",").join(sample.keySet()) + ") VALUES (" + StringUtils.repetition("?", ",", sample.keySet().size()) + ")";

            PreparedStatement statement = connection.prepareStatement(sql);

            for (Map<T, Object> record : records) {
                Collection<Object> objs = record.values();
                Iterator<Object> iterator = objs.iterator();

                for (int i = 1; i <= objs.size(); i++) {
                    statement.setObject(i, iterator.next());
                }

                statement.addBatch();
            }
            statement.executeBatch();

            connection.commit();
        }
    }
    
    public void insertOrReplaceBatch(List<Map<T, Object>> records) throws SQLException {

        if (!records.isEmpty()) {

            Map<T, Object> sample = records.get(0);
            String sql = "INSERT OR REPLACE INTO " + name + " (" + Joiner.on(",").join(sample.keySet()) + ") VALUES (" + StringUtils.repetition("?", ",", sample.keySet().size()) + ")";

            PreparedStatement statement = connection.prepareStatement(sql);

            for (Map<T, Object> record : records) {
                Collection<Object> objs = record.values();
                Iterator<Object> iterator = objs.iterator();

                for (int i = 1; i <= objs.size(); i++) {
                    statement.setObject(i, iterator.next());
                }

                statement.addBatch();
            }
            statement.executeBatch();

            connection.commit();
        }
    }
    
    public List<Map<T, Object>> select(T[] columns, Map<T, Object> criteria) throws SQLException {

        List<Map<T, Object>> list = new LinkedList<>();

        Set<T> keys = criteria.keySet();
        Iterator<T> iterator = keys.iterator();

        String[] clauses = new String[keys.size()];

        for (int i = 0; i < clauses.length; i++) {
            clauses[i] = iterator.next() + "=?";
        }

        String sql = "SELECT " + Joiner.on(",").join(columns) + " FROM " + name;
        if (!criteria.isEmpty()) {
            sql = sql + " WHERE " + Joiner.on(" AND ").join(clauses);
        }

        PreparedStatement statement = connection.prepareStatement(sql);

        Collection<Object> objs = criteria.values();
        Iterator<Object> iterator2 = objs.iterator();

        for (int i = 1; i <= objs.size(); i++) {
            statement.setObject(i, iterator2.next());
        }

        ResultSet results = statement.executeQuery();
        ResultSetMetaData metadata = results.getMetaData();
        int columnCount = metadata.getColumnCount();

        while (results.next()) {
            Map<T, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(mapping.get(metadata.getColumnName(i)), results.getObject(i));
            }
            list.add(row);
        }

        return list;
    }

    public List<Map<T, Object>> selectDistinct(T[] columns, Map<T, Object> criteria) throws SQLException {

        List<Map<T, Object>> list = new LinkedList<>();

        Set<T> keys = criteria.keySet();
        Iterator<T> iterator = keys.iterator();

        String[] clauses = new String[keys.size()];

        for (int i = 0; i < clauses.length; i++) {
            clauses[i] = iterator.next() + "=?";
        }

        String sql = "SELECT DISTINCT " + Joiner.on(",").join(columns) + " FROM " + name;
        if (!criteria.isEmpty()) {
            sql = sql + " WHERE " + Joiner.on(" AND ").join(clauses);
        }

        PreparedStatement statement = connection.prepareStatement(sql);

        Collection<Object> objs = criteria.values();
        Iterator<Object> iterator2 = objs.iterator();

        for (int i = 1; i <= objs.size(); i++) {
            statement.setObject(i, iterator2.next());
        }

        ResultSet results = statement.executeQuery();
        ResultSetMetaData metadata = results.getMetaData();
        int columnCount = metadata.getColumnCount();

        while (results.next()) {
            Map<T, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(mapping.get(metadata.getColumnName(i)), results.getObject(i));
            }
            list.add(row);
        }

        return list;
    }
    
    public void delete(Map<T, Object> criteria) throws SQLException {
        Set<T> keys = criteria.keySet();
        Iterator<T> iterator = keys.iterator();

        String[] clauses = new String[keys.size()];

        for (int i = 0; i < clauses.length; i++) {
            clauses[i] = iterator.next() + "=?";
        }

        String sql = "DELETE FROM " + name;
        if (!criteria.isEmpty()) {
            sql = sql + " WHERE " + Joiner.on(" AND ").join(clauses);
        }

        PreparedStatement statement = connection.prepareStatement(sql);

        Collection<Object> objs = criteria.values();
        Iterator<Object> iterator2 = objs.iterator();

        for (int i = 1; i <= objs.size(); i++) {
            statement.setObject(i, iterator2.next());
        }

        statement.executeUpdate();

        connection.commit();
    }    
    
}
