/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata;

import dev.meng.wikidata.analyze.Analyzer;
import dev.meng.wikidata.lib.db.SQLDB;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;
import dev.meng.wikidata.pagecount.Downloader;
import dev.meng.wikidata.pagecount.Parser;
import dev.meng.wikidata.util.string.StringUtils;
import dev.meng.wikidata.pageview.Consolidator;
import dev.meng.wikidata.simulation.Simulator;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author meng
 */
@Loggable(output=LogOutput.CONSOLE)
public class Main {
    
    public static void init(){
        System.out.println(DB.list());
        Scanner input = new Scanner(System.in);
        String line = input.nextLine();
        StringTokenizer tokenizer = new StringTokenizer(line);
        while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken();
            SQLDB db = DB.get(name);
            if(db==null){
                System.out.println("invalid choice");
            } else {
                try {
                    db.init();
                } catch (SQLException ex) {
                    Logger.log(Main.class, LogLevel.ERROR, ex);
                }
            }
        }
    }
    
    public static void download(){
        try {
            Scanner input = new Scanner(System.in);
            System.out.print("Start timestamp (format "+Configure.PAGECOUNT.TIMESTAMP_FORMAT+"): ");
            GregorianCalendar start = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
            System.out.print("End timestamp (format "+Configure.PAGECOUNT.TIMESTAMP_FORMAT+"): ");
            GregorianCalendar end = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
            Downloader downloader = new Downloader();
            downloader.download(start, end);
        } catch (ParseException ex) {
            Logger.log(Main.class, LogLevel.ERROR, ex);
        }
    }
    
    public static void parse(){
        try {
            Scanner input = new Scanner(System.in);
            System.out.print("Start timestamp (format "+Configure.PAGECOUNT.TIMESTAMP_FORMAT+"): ");
            GregorianCalendar start = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
            System.out.print("End timestamp (format "+Configure.PAGECOUNT.TIMESTAMP_FORMAT+"): ");
            GregorianCalendar end = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
            Parser parser = new Parser();
            parser.parse(start, end);
        } catch (ParseException ex) {
            Logger.log(Main.class, LogLevel.ERROR, ex);
        }
    }
    
    public static void consolidate(){
        Consolidator consolidator = new Consolidator();
        consolidator.consolidate();
    }
    
    public static void analyze(){
        try {
            Analyzer analyzer = new Analyzer();
            
            Scanner input = new Scanner(System.in);
            System.out.print("Type (frequency, fileusage, revision): ");
            String type = input.next();
            
            String lang;
            GregorianCalendar start;
            GregorianCalendar end;
            GregorianCalendar revStart;
            GregorianCalendar revEnd;
            int limit;
            
            switch(type){
                case "fileusage":
                    System.out.print("Lang: ");
                    lang = input.next();
                    System.out.print("Start timestamp (format " + Configure.PAGECOUNT.TIMESTAMP_FORMAT + "): ");
                    start = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
                    System.out.print("End timestamp (format " + Configure.PAGECOUNT.TIMESTAMP_FORMAT + "): ");
                    end = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
                    System.out.print("Limit: ");
                    limit = Integer.parseInt(input.next());
                    analyzer.analyzeFileusage(lang, start, end, limit);
                    break;
                case "revision":
                    System.out.print("Lang: ");
                    lang = input.next();
                    System.out.print("Start timestamp (format " + Configure.PAGECOUNT.TIMESTAMP_FORMAT + "): ");
                    start = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
                    System.out.print("End timestamp (format " + Configure.PAGECOUNT.TIMESTAMP_FORMAT + "): ");
                    end = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
                    System.out.print("Limit: ");
                    limit = Integer.parseInt(input.next());
                    System.out.print("Revision start timestamp (format " + Configure.PAGECOUNT.TIMESTAMP_FORMAT + "): ");
                    revStart = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
                    System.out.print("Revision end timestamp (format " + Configure.PAGECOUNT.TIMESTAMP_FORMAT + "): ");
                    revEnd = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);                    
                    analyzer.analyzeRevision(lang, start, end, limit, revStart, revEnd);
                    break;
                case "frequency":
                    System.out.print("Lang: ");
                    lang = input.next();
                    System.out.print("Start timestamp (format " + Configure.PAGECOUNT.TIMESTAMP_FORMAT + "): ");
                    start = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
                    System.out.print("End timestamp (format " + Configure.PAGECOUNT.TIMESTAMP_FORMAT + "): ");
                    end = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
                    System.out.print("Limit: ");
                    limit = Integer.parseInt(input.next());
                    System.out.println("Total frequency of selected pages: "+analyzer.analyzeTopFrequency(lang, start, end, limit));
                    System.out.println("Total frequency of all pages: "+analyzer.analyzeTotalFrequency(lang, start, end));
                    break;                    
            }
        } catch (ParseException ex) {
            Logger.log(Main.class, LogLevel.ERROR, ex);
        }
    }
    
    public static void simulate(){
        try {
            Simulator simulator = new Simulator();
            Scanner input = new Scanner(System.in);
            System.out.print("Lang: ");
            String lang = input.next();
            System.out.print("Start timestamp (format " + Configure.PAGECOUNT.TIMESTAMP_FORMAT + "): ");
            GregorianCalendar start = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
            System.out.print("End timestamp (format " + Configure.PAGECOUNT.TIMESTAMP_FORMAT + "): ");
            GregorianCalendar end = StringUtils.parseTimestamp(input.next(), Configure.PAGECOUNT.TIMESTAMP_FORMAT);
            simulator.simulate(lang, start, end);
        } catch (ParseException ex) {
            Logger.log(Main.class, LogLevel.ERROR, ex);
        }
    }
    
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        while(true){
            System.out.print("Command (init, download, parse, consolidate, analyze, simulate, exit): ");
            String command = input.next();
            switch(command){
                case "init":
                    init();
                    break;
                case "download":
                    download();
                    break;
                case "parse":
                    parse();
                    break;
                case "consolidate":
                    consolidate();
                    break;
                case "analyze":
                    analyze();
                    break;
                case "simulate":
                    simulate();
                    break;                    
                case "exit":
                    System.exit(0);
                    break;
            }
        }
    }
}
