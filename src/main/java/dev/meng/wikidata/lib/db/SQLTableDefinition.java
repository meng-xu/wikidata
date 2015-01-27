/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.lib.db;

/**
 *
 * @author xumeng
 */
public interface SQLTableDefinition{
    public SQLDataType getType();
    public String getName();
    public SQLColumnAttribute[] getAttributes();
}
