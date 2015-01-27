/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.metadata;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author meng
 */
public class FileInfo {
    private String title;
    private String lang;
    private long size;
    private Set<PageInfo> pages;

    public FileInfo(){
        pages = new HashSet<>();
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Set<PageInfo> getPages() {
        return pages;
    }

    public void setPages(Set<PageInfo> pages) {
        this.pages = pages;
    }
    
}