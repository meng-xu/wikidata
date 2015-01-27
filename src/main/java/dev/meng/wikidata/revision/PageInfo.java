/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.revision;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author meng
 */
public class PageInfo {
    private String pageId;
    private String lang;
    private String title;
    private String lastRevisionId;
    private long size;
    private Set<RevisionInfo> revisions;

    public PageInfo(){
        revisions = new HashSet<>();
    }
    
    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
    
    public String getLastRevisionId() {
        return lastRevisionId;
    }

    public void setLastRevisionId(String lastRevisionId) {
        this.lastRevisionId = lastRevisionId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Set<RevisionInfo> getRevisions() {
        return revisions;
    }

    public void setRevisions(Set<RevisionInfo> revisions) {
        this.revisions = revisions;
    }
}
