/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.lib.db;

/**
 *
 * @author meng
 */
public enum SQLColumnAttribute {
    PRIMARY_KEY("PRIMARY KEY", true),
    UNIQUE("UNIQUE", true),
    AUTOINCREMENT("AUTOINCREMENT", true);
    
    private String repr;
    private boolean group;
    
    private SQLColumnAttribute(String repr, boolean group){
        this.repr = repr;
        this.group = group;
    }
    
    public String getRepr(){
        return repr;
    }
    
    public boolean isGroup(){
        return group;
    }
}
