/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.simulation;

/**
 *
 * @author meng
 */
public class AccessRecord {
    private long id;
    private String lang;
    private String pageId;
    private long accessStart;
    private long accessEnd;

    public AccessRecord(long id, String lang, String pageId, long accessStart, long accessEnd) {
        this.id = id;
        this.lang = lang;
        this.pageId = pageId;
        this.accessStart = accessStart;
        this.accessEnd = accessEnd;
    }

    public long getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public String getPageId() {
        return pageId;
    }

    public long getAccessStart() {
        return accessStart;
    }

    public long getAccessEnd() {
        return accessEnd;
    }
    
    public String toString(){
        return lang+","+pageId+","+accessStart+","+accessEnd;
    }
}
