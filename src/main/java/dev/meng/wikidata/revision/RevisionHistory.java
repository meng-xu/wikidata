/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.revision;

import dev.meng.wikidata.Configure;
import dev.meng.wikidata.DB;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;
import dev.meng.wikidata.revision.db.Page;
import dev.meng.wikidata.revision.db.Revision;
import dev.meng.wikidata.util.string.StringConvertionException;
import dev.meng.wikidata.util.string.StringUtils;
import dev.meng.wikidata.util.http.HttpUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author meng
 */
@Loggable(output=LogOutput.FILE)
public class RevisionHistory {
    
    private Map<String, Map<String, PageInfo>> pages;
    
    public RevisionHistory(){
        pages = new HashMap<>();
    }
       
    public void queryRevisions(Set<PageInfo> pages, GregorianCalendar start, GregorianCalendar end){
        try {
            for(PageInfo page : pages){
                storePage(page);
                queryPageInfo(page.getLang(), page.getPageId(), page);
                queryRevisionList(page.getLang(), page.getPageId(), page, start, end);
            }

            
            List<Map<Page, Object>> pageRecords = new LinkedList<>();
            List<Map<Revision, Object>> revisionRecords = new LinkedList<>();
            
            for(PageInfo page : pages){
                Map<Page, Object> pageRecord = new HashMap<>();
                pageRecord.put(Page.LANG, page.getLang());
                pageRecord.put(Page.PAGE_ID, page.getPageId());
                pageRecord.put(Page.TITLE, page.getTitle());
                pageRecord.put(Page.SIZE, page.getSize());
                pageRecord.put(Page.LAST_REV_ID, page.getLastRevisionId());
                pageRecords.add(pageRecord);
            }
            DB.REVISION.PAGE.insertOrIgnoreBatch(pageRecords);
            
            List<Map<Page, Object>> updatedPages = DB.REVISION.PAGE.retrieveAll();
            Map<String, Integer> pageIdMap = new HashMap<>();
            for(Map<Page, Object> updatedPage : updatedPages){
                pageIdMap.put(updatedPage.get(Page.LANG)+"/"+updatedPage.get(Page.PAGE_ID), (Integer) updatedPage.get(Page.ID));
            }

            for(PageInfo page : pages){
                int pageId = pageIdMap.get(page.getLang()+"/"+page.getPageId());
                for(RevisionInfo revision : page.getRevisions()){
                    Map<Revision, Object> revisionRecord = new HashMap<>();
                    revisionRecord.put(Revision.PAGE_ID, pageId);
                    revisionRecord.put(Revision.REV_ID, revision.getRevId());
                    revisionRecord.put(Revision.TIMESTAMP, revision.getTimestamp().getTimeInMillis());
                    revisionRecords.add(revisionRecord);
                }
            }            
            DB.REVISION.REVISION.insertOrIgnoreBatch(revisionRecords);
            
        } catch (SQLException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        }
        
    }
    
    private void storePage(PageInfo page){
        Map<String, PageInfo> langMap = pages.get(page.getLang());
        if(langMap==null){
            langMap = new HashMap<>();
        }
        langMap.put(page.getPageId(), page);
        pages.put(page.getLang(), langMap);
    }

    private PageInfo getOrCreatePage(String lang, String pageId){
        Map<String, PageInfo> langMap = pages.get(lang);
        if(langMap==null){
            langMap = new HashMap<>();
            pages.put(lang, langMap);
        }
        PageInfo page = langMap.get(pageId);
        if(page==null){
            page = new PageInfo();
            page.setLang(lang);
            page.setPageId(pageId);
            langMap.put(pageId, page);
        }
        return page;
    }

    private void queryPageInfo(String lang, String pageId, PageInfo page){
        Map<String, Object> params = new HashMap<>();
        params.put("format", "json");
        params.put("action", "query");
        params.put("pageids", pageId);
        params.put("prop", "info");
        try {
            String urlString = String.format(Configure.REVISION.API_ENDPOINT, lang) + "?" + StringUtils.mapToURLParameters(params, Configure.REVISION.DEFAULT_ENCODING);
            URL url = new URL(urlString);

            JSONObject response = HttpUtils.queryForJSONResponse(url, Configure.REVISION.DEFAULT_ENCODING);
            try {
                JSONObject pageInfo = response.getJSONObject("query").getJSONObject("pages").getJSONObject(pageId);
                page.setTitle(pageInfo.getString("title"));
                page.setSize(pageInfo.getLong("length"));
                page.setLastRevisionId(Long.toString(pageInfo.getLong("lastrevid")));

            } catch (JSONException ex) {
                Logger.log(this.getClass(), LogLevel.WARNING, "Error in response: " + urlString + ", " + response.toString() + ", " + ex.getMessage());
            }

        } catch (UnsupportedEncodingException ex) {
            Logger.log(this.getClass(), LogLevel.WARNING, "Error in encoding: " + params.toString() + ", " + ex.getMessage());
        } catch (MalformedURLException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        } catch (IOException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        } catch (StringConvertionException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        }
    }

    private List<Map<String, Object>> queryRevisionListWorker(String lang, String pageId, String cont, String start, String end){
        Map<String, Object> params = new HashMap<>();
        params.put("format", "json");
        params.put("action", "query");
        params.put("pageids", pageId);
        params.put("prop", "revisions");
        params.put("rvprop", "ids|timestamp");
        params.put("rvstart", start);
        params.put("rvend", end);
        params.put("rvdir", "newer");
        if(cont!=null){
            params.put("rvcontinue", cont);
        }
        
        List<Map<String, Object>> result = new LinkedList<>();
        
        try {
            String urlString = String.format(Configure.REVISION.API_ENDPOINT, lang) + "?" + StringUtils.mapToURLParameters(params, Configure.REVISION.DEFAULT_ENCODING);            
            URL url = new URL(urlString);

            JSONObject response = HttpUtils.queryForJSONResponse(url, Configure.REVISION.DEFAULT_ENCODING);
            try {
                JSONObject pageRecord = response.getJSONObject("query").getJSONObject("pages").getJSONObject(pageId);
                if(pageRecord.has("revisions")){
                    JSONArray revisions = pageRecord.getJSONArray("revisions");

                    for(int i=0;i<revisions.length();i++){
                        JSONObject revision = revisions.getJSONObject(i);
                        Map<String, Object> record = new HashMap<>();
                        record.put("revid", Long.toString(revision.getLong("revid")));
                        record.put("timestamp", StringUtils.parseTimestamp(revision.getString("timestamp"), Configure.REVISION.TIMESTAMP_FORMAT));
                        result.add(record);
                    }
                }
                String queryContinue = null;
                if (response.has("query-continue")) {
                    queryContinue = response.getJSONObject("query-continue").getJSONObject("revisions").get("rvcontinue").toString();
                }

                if (queryContinue != null) {
                    List<Map<String, Object>> moreResult = queryRevisionListWorker(lang, pageId, queryContinue, start, end);
                    result.addAll(moreResult);
                }
            } catch (Exception ex) {
                Logger.log(this.getClass(), LogLevel.WARNING, "Error in response: " + urlString + ", " + response.toString() + ", " + ex.getMessage());
            }

        } catch (UnsupportedEncodingException ex) {
            Logger.log(this.getClass(), LogLevel.WARNING, "Error in encoding: "+params.toString()+", "+ex.getMessage());
        } catch (MalformedURLException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        } catch (IOException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        } catch (StringConvertionException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        }
        
        return result;
    }
    
    private void queryRevisionList(String lang, String pageId, PageInfo page, GregorianCalendar start, GregorianCalendar end){
        String startString = StringUtils.formatTimestamp(start, Configure.REVISION.TIMESTAMP_FORMAT);
        String endString = StringUtils.formatTimestamp(end, Configure.REVISION.TIMESTAMP_FORMAT);
        List<Map<String, Object>> revisions = queryRevisionListWorker(lang, pageId, null, startString, endString);
        for(Map<String, Object> revision : revisions){
            RevisionInfo revisionInfo = new RevisionInfo();
            revisionInfo.setRevId((String) revision.get("revid"));
            revisionInfo.setTimestamp((GregorianCalendar) revision.get("timestamp"));
            page.getRevisions().add(revisionInfo);
            revisionInfo.setPage(page);
        }
    }
}