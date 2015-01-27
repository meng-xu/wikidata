/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.metadata;

import com.google.common.base.Joiner;
import dev.meng.wikidata.Configure;
import dev.meng.wikidata.DB;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;
import dev.meng.wikidata.util.string.StringConvertionException;
import dev.meng.wikidata.util.string.StringUtils;
import dev.meng.wikidata.metadata.db.File;
import dev.meng.wikidata.metadata.db.Page;
import dev.meng.wikidata.metadata.db.PageFile;
import dev.meng.wikidata.metadata.db.Revision;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
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
public class Metadata {
    
    private Map<String, Map<String, PageInfo>> pages;
    private Map<String, Map<String, FileInfo>> files;
    
    public Metadata(){
        pages = new HashMap<>();
        files = new HashMap<>();
    }
       
    public void queryMetadata(Set<PageInfo> pages, GregorianCalendar start, GregorianCalendar end){
        try {
            for(PageInfo page : pages){
                storePage(page);
                queryPageInfo(page.getLang(), page.getPageId(), page);
                queryRevisionList(page.getLang(), page.getPageId(), page, start, end);
                queryFileList(page.getLang(), page.getPageId(), page);
            }
            Set<FileInfo> fileSet = new HashSet<>();
            for(String lang : files.keySet()){
                fileSet.addAll(files.get(lang).values());
            }
            for(FileInfo file : fileSet){
                queryFileInfo(file.getLang(), file.getTitle(), file);
            }
            
            List<Map<Page, Object>> pageRecords = new LinkedList<>();
            List<Map<File, Object>> fileRecords = new LinkedList<>();
            List<Map<Revision, Object>> revisionRecords = new LinkedList<>();
            List<Map<PageFile, Object>> pageFileRecords = new LinkedList<>();
            
            for(PageInfo page : pages){
                Map<Page, Object> pageRecord = new HashMap<>();
                pageRecord.put(Page.LANG, page.getLang());
                pageRecord.put(Page.PAGE_ID, page.getPageId());
                pageRecord.put(Page.TITLE, page.getTitle());
                pageRecord.put(Page.SIZE, page.getSize());
                pageRecord.put(Page.LAST_REV_ID, page.getLastRevisionId());
                pageRecords.add(pageRecord);
            }
            DB.METADATA.PAGE.insertOrIgnoreBatch(pageRecords);
            
            List<Map<Page, Object>> updatedPages = DB.METADATA.PAGE.retrieveAll();
            Map<String, Integer> pageIdMap = new HashMap<>();
            for(Map<Page, Object> updatedPage : updatedPages){
                pageIdMap.put(updatedPage.get(Page.LANG)+"/"+updatedPage.get(Page.PAGE_ID), (Integer) updatedPage.get(Page.ID));
            }
            
            for(FileInfo file : fileSet){
                Map<File, Object> fileRecord = new HashMap<>();
                fileRecord.put(File.LANG, file.getLang());
                fileRecord.put(File.TITLE, file.getTitle());
                fileRecord.put(File.SIZE, file.getSize());
                fileRecords.add(fileRecord);
            }
            DB.METADATA.FILE.insertOrIgnoreBatch(fileRecords);
            
            List<Map<File, Object>> updatedFiles = DB.METADATA.FILE.retrieveAll();
            Map<String, Integer> fileIdMap = new HashMap<>();
            for(Map<File, Object> updatedFile : updatedFiles){              
                fileIdMap.put(updatedFile.get(File.LANG)+"/"+updatedFile.get(File.TITLE), (Integer) updatedFile.get(File.ID));
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
                for(FileInfo file : page.getFiles()){                                  
                    int fileId = fileIdMap.get(file.getLang()+"/"+file.getTitle());
                    Map<PageFile, Object> pageFileRecord = new HashMap<>();
                    pageFileRecord.put(PageFile.PAGE_ID, pageId);
                    pageFileRecord.put(PageFile.FILE_ID, fileId);
                    pageFileRecords.add(pageFileRecord);
                }
            }            
            DB.METADATA.REVISION.insertOrIgnoreBatch(revisionRecords);
            DB.METADATA.PAGE_FILE.insertOrIgnoreBatch(pageFileRecords);
            
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
    
    private void storeFile(FileInfo file){
        Map<String, FileInfo> langMap = files.get(file.getLang());
        if(langMap==null){
            langMap = new HashMap<>();
        }
        langMap.put(file.getTitle(), file);
        files.put(file.getLang(), langMap);
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
    
    private FileInfo getOrCreateFile(String lang, String title){
        Map<String, FileInfo> langMap = files.get(lang);
        if(langMap==null){
            langMap = new HashMap<>();
            files.put(lang, langMap);
        }
        FileInfo file = langMap.get(title);
        if(file==null){
            file = new FileInfo();
            file.setLang(lang);
            file.setTitle(title);
            langMap.put(title, file);
        }
        return file;
    }
    
    private PageInfo getPage(String lang, String pageId){
        Map<String, PageInfo> langMap = pages.get(lang);
        if(langMap==null){
            return null;
        } else{
            return langMap.get(pageId);
        }
    }
    
    private FileInfo getFile(String lang, String title){
        Map<String, FileInfo> langMap = files.get(lang);
        if(langMap==null){
            return null;
        } else{
            return langMap.get(title);
        }
    }
    
    private void queryPageInfo(String lang, String pageId, PageInfo page){
        Map<String, Object> params = new HashMap<>();
        params.put("format", "json");
        params.put("action", "query");
        params.put("pageids", pageId);
        params.put("prop", "info");
        try {
            String urlString = String.format(Configure.METADATA.API_ENDPOINT, lang) + "?" + StringUtils.mapToURLParameters(params, Configure.METADATA.DEFAULT_ENCODING);
            URL url = new URL(urlString);

            JSONObject response = queryForJSONResponse(url);
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
    
    private List<Map<String, Object>> queryFileListWorker(String lang, String pageId, String cont){
        Map<String, Object> params = new HashMap<>();
        params.put("format", "json");
        params.put("action", "query");
        params.put("pageids", pageId);
        params.put("prop", "images");
        if(cont!=null){
            params.put("imcontinue", cont);
        }
        
        List<Map<String, Object>> result = new LinkedList<>();
        
        try {
            String urlString = String.format(Configure.METADATA.API_ENDPOINT, lang) + "?" + StringUtils.mapToURLParameters(params, Configure.METADATA.DEFAULT_ENCODING);            
            URL url = new URL(urlString);

            JSONObject response = queryForJSONResponse(url);
            try {
                JSONObject pageRecord = response.getJSONObject("query").getJSONObject("pages").getJSONObject(pageId);
                if(pageRecord.has("images")){
                    JSONArray images = pageRecord.getJSONArray("images");

                    for(int i=0;i<images.length();i++){
                        JSONObject image = images.getJSONObject(i);
                        Map<String, Object> record = new HashMap<>();
                        record.put("title", image.getString("title"));
                        result.add(record);
                    }
                }
                String queryContinue = null;
                if(response.has("query-continue")){
                    queryContinue = response.getJSONObject("query-continue").getJSONObject("images").getString("imcontinue");
                }
                
                if(queryContinue!=null){
                    List<Map<String, Object>> moreResult = queryFileListWorker(lang, pageId, queryContinue);
                    result.addAll(moreResult);
                }
                
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
        
        return result;
    }
    
    private void queryFileList(String lang, String pageId, PageInfo page){
        List<Map<String, Object>> files = queryFileListWorker(lang, pageId, null);
        for(Map<String, Object> file : files){
            FileInfo fileInfo = getOrCreateFile(lang, (String) file.get("title"));
            page.getFiles().add(fileInfo);
            fileInfo.getPages().add(page);
        }
    }
    
    private void queryFileInfo(String lang, String title, FileInfo file){
        Map<String, Object> params = new HashMap<>();
        params.put("format", "json");
        params.put("action", "query");
        params.put("titles", title);
        params.put("prop", "imageinfo");
        params.put("iiprop", "size");
        try {
            String urlString = String.format(Configure.METADATA.API_ENDPOINT, lang) + "?" + StringUtils.mapToURLParameters(params, Configure.METADATA.DEFAULT_ENCODING);            

            URL url = new URL(urlString);

            JSONObject response = queryForJSONResponse(url);
            try {
                JSONObject pageMap = response.getJSONObject("query").getJSONObject("pages");
                JSONObject pageRecord = pageMap.getJSONObject((String) pageMap.keys().next());
                if(pageRecord.has("imageinfo")){
                    JSONArray fileInfoList = pageRecord.getJSONArray("imageinfo");   
                    file.setSize(fileInfoList.getJSONObject(0).getLong("size"));
                } else{
                    file.setSize(0L);
                }
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
    
    private List<Map<String, Object>> queryFileUsageWorker(String lang, String title, String cont){
        Map<String, Object> params = new HashMap<>();
        params.put("format", "json");
        params.put("action", "query");
        params.put("titles", title);
        params.put("prop", "fileusage");
        params.put("fuprop", "pageid|title");
        if(cont!=null){
            params.put("fucontinue", cont);
        }
        
        List<Map<String, Object>> result = new LinkedList<>();
        
        try {
            String urlString = String.format(Configure.METADATA.API_ENDPOINT, lang) + "?" + StringUtils.mapToURLParameters(params, Configure.METADATA.DEFAULT_ENCODING);            

            URL url = new URL(urlString);

            JSONObject response = queryForJSONResponse(url);
            try {
                JSONObject pageMap = response.getJSONObject("query").getJSONObject("pages");
                JSONObject pageRecord = pageMap.getJSONObject((String) pageMap.keys().next());
                if(pageRecord.has("fileusage")){
                    JSONArray pages = pageRecord.getJSONArray("fileusage");   
                    
                    for(int i=0;i<pages.length();i++){
                        Map<String, Object> record = new HashMap<>();
                        record.put("pageid", Long.toString(pages.getJSONObject(i).getLong("pageid")));
                        record.put("title", pages.getJSONObject(i).getString("title"));
                        result.add(record);
                    }
                }
                
                String queryContinue = null;
                if(response.has("query-continue")){
                    queryContinue = response.getJSONObject("query-continue").getJSONObject("fileusage").getString("fucontinue");
                }
                
                if(queryContinue!=null){
                    List<Map<String, Object>> moreResult = queryFileUsageWorker(lang, title, queryContinue);
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
    
//    public void queryFileUsage(String lang, String title, FileInfo file){
//        List<Map<String, Object>> pages = queryFileUsageWorker(lang, title, null);
//        for(Map<String, Object> page : pages){
//            PageInfo pageInfo = new PageInfo();
//            pageInfo.setLang(lang);
//            pageInfo.setTitle((String) page.get("title"));
//            pageInfo.setPageId((String) page.get("pageid"));
//            file.getPages().add(pageInfo);
//        }
//    }    
    
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
            String urlString = String.format(Configure.METADATA.API_ENDPOINT, lang) + "?" + StringUtils.mapToURLParameters(params, Configure.METADATA.DEFAULT_ENCODING);            
            URL url = new URL(urlString);

            JSONObject response = queryForJSONResponse(url);
            try {
                JSONObject pageRecord = response.getJSONObject("query").getJSONObject("pages").getJSONObject(pageId);
                if(pageRecord.has("revisions")){
                    JSONArray revisions = pageRecord.getJSONArray("revisions");

                    for(int i=0;i<revisions.length();i++){
                        JSONObject revision = revisions.getJSONObject(i);
                        Map<String, Object> record = new HashMap<>();
                        record.put("revid", Long.toString(revision.getLong("revid")));
                        record.put("timestamp", StringUtils.parseTimestamp(revision.getString("timestamp"), Configure.METADATA.TIMESTAMP_FORMAT));
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
        String startString = StringUtils.formatTimestamp(start, Configure.METADATA.TIMESTAMP_FORMAT);
        String endString = StringUtils.formatTimestamp(end, Configure.METADATA.TIMESTAMP_FORMAT);
        List<Map<String, Object>> revisions = queryRevisionListWorker(lang, pageId, null, startString, endString);
        for(Map<String, Object> revision : revisions){
            RevisionInfo revisionInfo = new RevisionInfo();
            revisionInfo.setRevId((String) revision.get("revid"));
            revisionInfo.setTimestamp((GregorianCalendar) revision.get("timestamp"));
            page.getRevisions().add(revisionInfo);
            revisionInfo.setPage(page);
        }
    }
    
    public Map<String, String>[] queryPageIds(String lang, List<String> titles){
        Map<String, String> norm = new HashMap<>();
        Map<String, String> result = new HashMap<>();
        
        int last = 0;
        int sizeCount = 0;

        for(int current = 0;current<titles.size();current++){
            String currentString = titles.get(current);
            sizeCount = sizeCount + currentString.length();
            if(currentString.contains("|") || currentString.contains("%7C") || currentString.contains("\\x7C") || currentString.length()>Configure.METADATA.REQUEST_LENGTH_MAX){
                Map<String, String>[] oneResult = queryPageIdsBatch(lang, titles.subList(last, current));
                norm.putAll(oneResult[0]);
                result.putAll(oneResult[1]);
                
                last = current+1;
                sizeCount = 0;
                
                norm.put(currentString, currentString);
                Logger.log(this.getClass(), LogLevel.WARNING, lang+" "+currentString+" invalid for query");
            } else if(sizeCount>Configure.METADATA.REQUEST_LENGTH_MAX){
                Map<String, String>[] oneResult = queryPageIdsBatch(lang, titles.subList(last, current));
                norm.putAll(oneResult[0]);
                result.putAll(oneResult[1]);
                
                last = current;
                sizeCount = currentString.length();
            }
        }
        
        if (last < titles.size()) {
            Map<String, String>[] oneResult = queryPageIdsBatch(lang, titles.subList(last, titles.size()));
            norm.putAll(oneResult[0]);
            result.putAll(oneResult[1]);
        }
        
        return new Map[]{norm, result};
    }
    
    private Map<String, String>[] queryPageIdsBatch(String lang, List<String> titles){
        if(!titles.isEmpty()){
            String titleString = Joiner.on("|").join(titles);
            return queryPageIdsBatchWorker(lang, titleString, null);
        } else{
            return new HashMap[]{new HashMap<>(), new HashMap<>()};
        }
    }
    
    private Map<String, String>[] queryPageIdsBatchWorker(String lang, String titles, String cont){
        Map<String, Object> params = new HashMap<>();
        params.put("format", "json");
        params.put("action", "query");
        params.put("titles", titles);
        params.put("prop", "info");
        if(cont!=null){
            params.put("incontinue", cont);
        }
        
        Map<String, String> norm = new HashMap<>();
        Map<String, String> result = new HashMap<>();
        
        try {
            String urlString = String.format(Configure.METADATA.API_ENDPOINT, lang) + "?" + StringUtils.mapToURLParameters(params, Configure.METADATA.DEFAULT_ENCODING);            
            URL url = new URL(urlString);

            JSONObject response = queryForJSONResponse(url);
            
            if(response!=null && response.has("query")){
                JSONObject responseQuery = response.getJSONObject("query");
                if(responseQuery.has("normalized")){
                    JSONArray normalizations = responseQuery.getJSONArray("normalized");
                    for(int i=0;i<normalizations.length();i++){
                        JSONObject normalization = normalizations.getJSONObject(i);
                        norm.put(normalization.getString("from"), normalization.getString("to"));
                    }
                }
                if(responseQuery.has("pages")){
                    JSONObject pages = responseQuery.getJSONObject("pages");
                    for(String pageKey : (Set<String>)pages.keySet()){
                        if(Long.parseLong(pageKey)>0){
                            JSONObject page = pages.getJSONObject(pageKey);
                            if(page.has("ns") && page.getLong("ns")==0L){
                                result.put(page.getString("title"), Long.toString(page.getLong("pageid")));
                            }
                        }
                    }
                }

                String queryContinue = null;
                if (response.has("query-continue")) {
                    queryContinue = response.getJSONObject("query-continue").getJSONObject("info").getString("incontinue");
                }

                if (queryContinue != null) {
                    Map<String, String>[] moreResult = queryPageIdsBatchWorker(lang, titles, queryContinue);
                    norm.putAll(moreResult[0]);
                    result.putAll(moreResult[1]);
                }
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

        return new Map[]{norm, result};
    }
    
    private JSONObject queryForJSONResponse(URL url) throws ProtocolException, IOException, StringConvertionException {
        JSONObject response = null;
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        
        if (connection.getResponseCode() == 200) {
            response = new JSONObject(StringUtils.inputStreamToString(connection.getInputStream(), Configure.METADATA.DEFAULT_ENCODING));
        } else {
            Logger.log(this.getClass(), LogLevel.WARNING, "Error in opening: " + url + ", " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
        
        return response;
    }    
}