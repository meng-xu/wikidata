/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata;

import dev.meng.wikidata.lib.db.SQLDB;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;
import dev.meng.wikidata.pagecount.Downloader;
import dev.meng.wikidata.pagecount.Parser;
import dev.meng.wikidata.util.string.StringUtils;
import dev.meng.wikidata.pageview.Consolidator;
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
    
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        while(true){
            System.out.print("Command (init, download, parse, consolidate, analyze, clean, exit): ");
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
//                case "analyze":
//                    analyze();
//                    break;
//                case "clean":
//                    clean();
//                    break;
                case "exit":
                    System.exit(0);
                    break;
            }
        }
    }
}
