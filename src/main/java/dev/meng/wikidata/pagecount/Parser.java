/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.pagecount;

import dev.meng.wikidata.Configure;
import dev.meng.wikidata.DB;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;
import dev.meng.wikidata.pagecount.db.Page;
import dev.meng.wikidata.pagecount.db.Processing;
import dev.meng.wikidata.pagecount.db.Summary;
import dev.meng.wikidata.pagecount.db.View;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author meng
 */
@Loggable(output=LogOutput.FILE)
public class Parser {

    public void parse(GregorianCalendar start, GregorianCalendar end){
        GregorianCalendar current = start;
        while(current.getTimeInMillis()<=end.getTimeInMillis()){
            parse(current);
            current.add(GregorianCalendar.HOUR_OF_DAY, 1);
        }
    }

    public void parse(GregorianCalendar timestamp) {
        Path filepath = PagecountUtils.timestampToFilepath(timestamp);
        Logger.log(this.getClass(), LogLevel.INFO, filepath.getFileName() + " parsing");

        long timestampValue = timestamp.getTimeInMillis();

        try {
            if (!DB.PAGECOUNT.PROCESSING.isProcessed(timestampValue)) {
                Scanner input = new Scanner(new GZIPInputStream(new FileInputStream(filepath.toFile())), StandardCharsets.UTF_8.name());

                Map<String, Map<Summary, Object>> summary = new HashMap<>();
                List<Map<Page, Object>> pageRecords = new LinkedList<>();
                List<Map<View, Object>> viewRecords = new LinkedList<>();

                while (input.hasNext()) {
                    String line = input.nextLine();
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    try {
                        String lang = tokenizer.nextToken();
                        String title = tokenizer.nextToken();
                        long frequency = Long.parseLong(tokenizer.nextToken());
                        long size = Long.parseLong(tokenizer.nextToken());

                        if (!lang.contains(".")) {
                            if (frequency > Configure.PAGECOUNT.FREQUENCY_THRESHOLD) {
                                Map<Page, Object> pageRecord = new HashMap<>();
                                pageRecord.put(Page.LANG, lang);
                                pageRecord.put(Page.TITLE, title);
                                pageRecord.put(Page.CONSOLIDATED, false);
                                pageRecords.add(pageRecord);

                                Map<View, Object> viewRecord = new HashMap<>();
                                viewRecord.put(View.PAGE_ID, pageRecord);
                                viewRecord.put(View.TIMESTAMP, timestampValue);
                                viewRecord.put(View.FREQUENCY, frequency);
                                viewRecord.put(View.SIZE, size);
                                viewRecords.add(viewRecord);
                            }

                            Map<Summary, Object> summaryRecord = summary.get(lang);
                            if (summaryRecord == null) {
                                summaryRecord = new HashMap<>();
                                summaryRecord.put(Summary.LANG, lang);
                                summaryRecord.put(Summary.TIMESTAMP, timestampValue);
                                summaryRecord.put(Summary.FREQUENCY, 0L);
                                summaryRecord.put(Summary.SIZE, 0L);
                            }
                            summaryRecord.put(Summary.FREQUENCY, (long) summaryRecord.get(Summary.FREQUENCY) + frequency);
                            summaryRecord.put(Summary.SIZE, (long) summaryRecord.get(Summary.SIZE) + size);
                        }
                    } catch (NoSuchElementException ex) {
                        Logger.log(this.getClass(), LogLevel.WARNING, "Unrecognized line: " + line);
                    }
                }

                DB.PAGECOUNT.SUMMARY.insertOrIgnoreBatch(new LinkedList<>(summary.values()));
                DB.PAGECOUNT.PAGE.insertOrIgnoreBatch(pageRecords);

                List<Map<Page, Object>> updatedPageRecords = DB.PAGECOUNT.PAGE.retrieveAll();

                Map<String, Map<Page, Object>> updatedPageRecordMap = new HashMap<>();

                for (Map<Page, Object> updatedPageRecord : updatedPageRecords) {
                    updatedPageRecordMap.put(updatedPageRecord.get(Page.LANG) + "/" + updatedPageRecord.get(Page.TITLE), updatedPageRecord);
                }

                for (Map<View, Object> viewRecord : viewRecords) {
                    Map<Page, Object> pageRecord = (Map<Page, Object>) viewRecord.get(View.PAGE_ID);
                    Map<Page, Object> updatedPageRecord = updatedPageRecordMap.get(pageRecord.get(Page.LANG) + "/" + pageRecord.get(Page.TITLE));
                    if (updatedPageRecord != null && updatedPageRecord.get(Page.LANG).equals(pageRecord.get(Page.LANG)) && updatedPageRecord.get(Page.TITLE).equals(pageRecord.get(Page.TITLE))) {
                        viewRecord.put(View.PAGE_ID, updatedPageRecord.get(Page.ID));
                    } else {
                        Logger.log(this.getClass(), LogLevel.WARNING, "Error handling data: " + pageRecord.get(Page.LANG) + " " + pageRecord.get(Page.TITLE) + " " + viewRecord.get(View.FREQUENCY) + " " + viewRecord.get(View.SIZE) + " " + updatedPageRecord);
                        viewRecords.remove(viewRecord);
                    }
                }

                DB.PAGECOUNT.VIEW.insertOrIgnoreBatch(viewRecords);

                Map<Processing, Object> processingRecord = new HashMap<>();
                processingRecord.put(Processing.TIMESTAMP, timestampValue);
                processingRecord.put(Processing.PROCESSING, new Date().getTime());
                DB.PAGECOUNT.PROCESSING.insert(processingRecord);
            } else{
                Logger.log(this.getClass(), LogLevel.INFO, filepath.getFileName()+" is already processed");
            }
        } catch (FileNotFoundException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        } catch (IOException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        } catch (SQLException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        }
    }
}