/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.pagecount;

import dev.meng.wikidata.Configure;
import dev.meng.wikidata.lib.log.LogLevel;
import dev.meng.wikidata.lib.log.LogOutput;
import dev.meng.wikidata.lib.log.Loggable;
import dev.meng.wikidata.lib.log.Logger;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.GregorianCalendar;

/**
 *
 * @author meng
 */
@Loggable(output=LogOutput.FILE)
public class Downloader {
            
    public void download(GregorianCalendar start, GregorianCalendar end){
        GregorianCalendar current = start;
        while(current.getTimeInMillis()<=end.getTimeInMillis()){
            download(current);
            current.add(GregorianCalendar.HOUR_OF_DAY, 1);
        }
    }
    
    private void download(GregorianCalendar timestamp){
        Path filepath = PagecountUtils.timestampToFilepath(timestamp);
        if(!filepath.toFile().exists() || filepath.toFile().isDirectory()){
            Logger.log(this.getClass(), LogLevel.INFO, filepath.getFileName()+" downloading");
            downloadWorker(0, timestamp, filepath);
        } else{
            Logger.log(this.getClass(), LogLevel.INFO, filepath.getFileName()+" already exists in repository");
        }
    }

    private void downloadWorker(int probing, GregorianCalendar timestamp, Path filepath){
        GregorianCalendar probingTimestamp = (GregorianCalendar) timestamp.clone();
        probingTimestamp.add(GregorianCalendar.SECOND, probing);
        try{
            URL url = PagecountUtils.timestampToURL(probingTimestamp);
            try {
                InputStream input = url.openStream();
                ReadableByteChannel channel = Channels.newChannel(input);
                FileOutputStream output = new FileOutputStream(filepath.toFile());
                output.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
                output.close();
                channel.close();
            } catch (IOException ex) {
                if(probing==-60){
                    Logger.log(this.getClass(), LogLevel.INFO, filepath.getFileName()+" downloading failed");
                } else if(probing>=0){
                    probing = probing + 1;
                } else{
                    probing = probing - 1;
                }
                try {
                    Thread.sleep(Configure.PAGECOUNT.HTTP_WAIT_TIME);
                } catch (InterruptedException ex1) {
                    Logger.log(this.getClass(), LogLevel.ERROR, ex1);
                }
                downloadWorker(probing, timestamp, filepath);
            }
        } catch (MalformedURLException ex) {
            Logger.log(this.getClass(), LogLevel.ERROR, ex);
        }
    }
}
