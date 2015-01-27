/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.pagecount;

import dev.meng.wikidata.Configure;
import dev.meng.wikidata.util.string.StringUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.GregorianCalendar;

/**
 *
 * @author meng
 */
public class PagecountUtils {
    private static String timestampToName(GregorianCalendar timestamp){
        return String.format(Configure.PAGECOUNT.FILENAME_FORMAT, StringUtils.formatTimestamp(timestamp, Configure.PAGECOUNT.TIMESTAMP_FORMAT));
    }
    public static Path timestampToFilepath(GregorianCalendar timestamp){
        String filename = timestampToName(timestamp);
        return Paths.get(Configure.PAGECOUNT.DATA_REPOSITORY, filename);
    }
    public static URL timestampToURL(GregorianCalendar timestamp) throws MalformedURLException{
        return new URL(Configure.PAGECOUNT.DOWNLOAD_SOURCE+StringUtils.formatTimestamp(timestamp, "yyyy")+"/"+StringUtils.formatTimestamp(timestamp, "yyyy-MM")+"/"+timestampToName(timestamp));
    }
}
