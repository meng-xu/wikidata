/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.pageview;

import com.google.common.base.Joiner;
import dev.meng.wikidata.Configure;
import dev.meng.wikidata.DB;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;
import dev.meng.wikidata.util.string.AsciiToUnicodeFormat;
import dev.meng.wikidata.util.string.CodecUtils;
import dev.meng.wikidata.util.string.StringConvertionException;
import dev.meng.wikidata.pageview.db.Page;
import dev.meng.wikidata.pageview.db.View;
import dev.meng.wikidata.util.http.HttpUtils;
import dev.meng.wikidata.util.string.StringUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author meng
 */
@Loggable(output=LogOutput.FILE)
public class Consolidator {
    
    public void consolidate(){
        try {
            List<String> langs = DB.PAGECOUNT.PAGE.retrieveAllLangs();
            
            for(String lang : langs){
                Logger.log(this.getClass(), LogLevel.INFO, "Consolidating "+lang);
                
                List<Map<Page, Object>> newPageRecords = new LinkedList<>();
                Map<String, List<Integer>> newToOldPageIdMap = new HashMap<>();
                
                List<String> titles = new LinkedList<>();
                Map<String, Integer> oldTitleToOldIdMap = new HashMap<>();
                
                List<Map<dev.meng.wikidata.pagecount.db.Page, Object>> oldPageRecords = DB.PAGECOUNT.PAGE.retrieveUnconsolidatedRecordsByLang(lang);
                for (Map<dev.meng.wikidata.pagecount.db.Page, Object> record : oldPageRecords) {
                    titles.add((String) record.get(dev.meng.wikidata.pagecount.db.Page.TITLE));
                    oldTitleToOldIdMap.put((String)record.get(dev.meng.wikidata.pagecount.db.Page.TITLE), (int) record.get(dev.meng.wikidata.pagecount.db.Page.ID));
                }
                
                Map<String, List<String>> titleMap = new HashMap<>();
                for(String title : titles){
                    String urlDecoded = title;
                    if(title.contains("%")){
                        urlDecoded = CodecUtils.asciiToUnicode(title, AsciiToUnicodeFormat.PERCENTAGE_HEX);
                    } 
                    String decoded = urlDecoded;
                    if(urlDecoded.contains("\\x")){
                        decoded = CodecUtils.asciiToUnicode(urlDecoded, AsciiToUnicodeFormat.SLASH_X_HEX);
                    }
                    List<String> encodedTitleList = titleMap.get(decoded);
                    if(encodedTitleList==null){
                        encodedTitleList = new LinkedList<>();
                    }
                    encodedTitleList.add(title);
                    titleMap.put(decoded, encodedTitleList);
                }
                
                List<String> decodedTitles = new LinkedList<>(titleMap.keySet());
                
                Map<String, String>[] queryResult = queryPageIds(lang, decodedTitles);
                Map<String, String> norms = queryResult[0];
                Map<String, String> pageIds = queryResult[1];
                
                for(String title : decodedTitles){
                    String normalizedTitle = norms.get(title);
                    String newPageId = pageIds.get(normalizedTitle);
                    if(newPageId!=null){
                        List<Integer> oldPageIdList = new LinkedList<>();
                        for(String oldTitle : titleMap.get(title)){
                            oldPageIdList.add(oldTitleToOldIdMap.get(oldTitle));
                        }
                        List<Integer> oldPageIds = newToOldPageIdMap.get(newPageId);
                        if(oldPageIds==null){
                            oldPageIds = new LinkedList<>();
                            
                            Map<Page, Object> pageRecord = new HashMap<>();
                            pageRecord.put(Page.PAGE_ID, newPageId);
                            pageRecord.put(Page.TITLE, normalizedTitle);
                            pageRecord.put(Page.LANG, lang);
                            newPageRecords.add(pageRecord);                            
                        } else{
                            Logger.log(this.getClass(), LogLevel.WARNING, "Mapping of same page ids: existing: "+oldPageIds.toString()+" and "+oldPageIdList.toString()+", new: "+newPageId);
                        }
                        oldPageIds.addAll(oldPageIdList);
                        newToOldPageIdMap.put(newPageId, oldPageIds);
                    }
                }
                
                DB.PAGEVIEW.PAGE.insertOrIgnoreBatch(newPageRecords);
                
                Map<String, Integer> newPageIdToIdMap = DB.PAGEVIEW.PAGE.retrievePageIdToIdMapByUnique(lang, newPageRecords);
                
                List<Map<View, Object>> newViewRecords = new LinkedList<>();
                
                for(String newPageId : newPageIdToIdMap.keySet()){
                    int newId = newPageIdToIdMap.get(newPageId);
                    Map<Long, Map<View, Object>> newViewRecordsById = new HashMap<>();
                
                    for(int oldId : newToOldPageIdMap.get(newPageId)){
                        List<Map<dev.meng.wikidata.pagecount.db.View, Object>> oldViewRecords = DB.PAGECOUNT.VIEW.retrieveByPageId(oldId);
                        for(Map<dev.meng.wikidata.pagecount.db.View, Object> oldViewRecord : oldViewRecords){
                            long timestamp = (long) oldViewRecord.get(dev.meng.wikidata.pagecount.db.View.TIMESTAMP);
                            Map<View, Object> newViewRecordById = newViewRecordsById.get(timestamp);
                            if(newViewRecordById==null){
                                newViewRecordById = new HashMap<>();
                                newViewRecordById.put(View.PAGE_ID, newId);
                                newViewRecordById.put(View.TIMESTAMP, timestamp);
                                newViewRecordById.put(View.FREQUENCY, 0L);
                                newViewRecordById.put(View.SIZE, 0L);
                            }
                            long newFrequency = (long)newViewRecordById.get(View.FREQUENCY) + ((Number)oldViewRecord.get(dev.meng.wikidata.pagecount.db.View.FREQUENCY)).longValue();
                            long newSize = (long)newViewRecordById.get(View.SIZE) + ((Number)oldViewRecord.get(dev.meng.wikidata.pagecount.db.View.SIZE)).longValue();
                            newViewRecordById.put(View.FREQUENCY, newFrequency);
                            newViewRecordById.put(View.SIZE, newSize);
                            newViewRecordsById.put(timestamp, newViewRecordById);
                        }
                    }   
                    newViewRecords.addAll(newViewRecordsById.values());
                }
                
                DB.PAGEVIEW.VIEW.insertOrIgnoreBatch(newViewRecords);
                
                for (Map<dev.meng.wikidata.pagecount.db.Page, Object> record : oldPageRecords) {
                    record.put(dev.meng.wikidata.pagecount.db.Page.CONSOLIDATED, true);
                }
                
                DB.PAGECOUNT.PAGE.insertOrReplaceBatch(oldPageRecords);
            }
        } catch (SQLException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        } catch (StringConvertionException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        }
    }
    
    private Map<String, String>[] queryPageIds(String lang, List<String> titles){
        Map<String, String> norm = new HashMap<>();
        Map<String, String> result = new HashMap<>();
        
        int last = 0;
        int sizeCount = 0;

        for(int current = 0;current<titles.size();current++){
            String currentString = titles.get(current);
            sizeCount = sizeCount + currentString.length();
            if(currentString.contains("|") || currentString.contains("%7C") || currentString.contains("\\x7C") || currentString.length()>Configure.PAGEVIEW.REQUEST_LENGTH_MAX){
                Map<String, String>[] oneResult = queryPageIdsBatch(lang, titles.subList(last, current));
                norm.putAll(oneResult[0]);
                result.putAll(oneResult[1]);
                
                last = current+1;
                sizeCount = 0;
                
                norm.put(currentString, currentString);
                Logger.log(this.getClass(), LogLevel.WARNING, lang+" "+currentString+" invalid for query");
            } else if(sizeCount>Configure.PAGEVIEW.REQUEST_LENGTH_MAX){
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
            String urlString = String.format(Configure.PAGEVIEW.API_ENDPOINT, lang) + "?" + StringUtils.mapToURLParameters(params, Configure.PAGEVIEW.DEFAULT_ENCODING);            
            URL url = new URL(urlString);

            JSONObject response = HttpUtils.queryForJSONResponse(url, Configure.FILEUSAGE.DEFAULT_ENCODING);
            
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
}
