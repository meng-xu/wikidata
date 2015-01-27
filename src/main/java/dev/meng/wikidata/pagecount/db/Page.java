/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.pagecount.db;

import dev.meng.wikidata.lib.db.SQLColumnAttribute;
import dev.meng.wikidata.lib.db.SQLDataType;
import dev.meng.wikidata.lib.db.SQLTableDefinition;

/**
 *
 * @author xumeng
 */
public enum Page implements SQLTableDefinition{
    
    ID(SQLDataType.INTEGER, new SQLColumnAttribute[]{SQLColumnAttribute.PRIMARY_KEY}),
    LANG(SQLDataType.TEXT, new SQLColumnAttribute[]{SQLColumnAttribute.UNIQUE}),
    TITLE(SQLDataType.TEXT, new SQLColumnAttribute[]{SQLColumnAttribute.UNIQUE}),
    CONSOLIDATED(SQLDataType.BOOLEAN, new SQLColumnAttribute[]{});

    private SQLDataType type;
    private SQLColumnAttribute[] attributes;
    
    private Page(SQLDataType type, SQLColumnAttribute[] attributes){
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
