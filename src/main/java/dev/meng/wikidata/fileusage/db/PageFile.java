/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.fileusage.db;

import dev.meng.wikidata.lib.db.SQLColumnAttribute;
import dev.meng.wikidata.lib.db.SQLDataType;
import dev.meng.wikidata.lib.db.SQLTableDefinition;

/**
 *
 * @author xumeng
 */
public enum PageFile implements SQLTableDefinition{
    
    PAGE_ID(SQLDataType.INTEGER, new SQLColumnAttribute[]{SQLColumnAttribute.PRIMARY_KEY}),
    FILE_ID(SQLDataType.INTEGER, new SQLColumnAttribute[]{SQLColumnAttribute.PRIMARY_KEY});

    private SQLDataType type;
    private SQLColumnAttribute[] attributes;
    
    private PageFile(SQLDataType type, SQLColumnAttribute[] attributes){
        this.type = type;
        this.attributes = attributes;
    }

    @Override
    public SQLDataType getType() {
        return type;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public SQLColumnAttribute[] getAttributes() {
        return this.attributes;
    }
}
