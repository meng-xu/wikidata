/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.util.string;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author xumeng
 */
public class StringUtils {

    public static String repetition(String base, String delimeter, int times){
        String result = "";
        if(times>0){
            result = result + base;
            for(int i=1;i<times;i++){
                result = result + delimeter + base;
            }
        }
        return result;
    }
    
    public static String formatTimestamp(GregorianCalendar date, String format){
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date.getTime());
    }
    
    public static GregorianCalendar parseTimestamp(String string, String format) throws ParseException {
        GregorianCalendar result = null;

        SimpleDateFormat formatter = new SimpleDateFormat(format);
        result = (GregorianCalendar) GregorianCalendar.getInstance();
        result.setTime(formatter.parse(string));

        return result;
    }
    
    public static String mapToURLParameters(Map<String, Object> params, String encoding) throws StringConvertionException{
        String result = "";
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            try {
                Map.Entry<String, Object> pair = (Map.Entry) it.next();
                result += URLEncoder.encode(pair.getKey(), encoding) + "="
                        + URLEncoder.encode(pair.getValue().toString(), encoding);
                if (it.hasNext()) {
                    result += "&";
                }
            } catch (UnsupportedEncodingException ex) {
                throw new StringConvertionException(ex);
            }
        }
        return result;
    }
    
    public static String inputStreamToString(InputStream input, String encoding) throws StringConvertionException{
        try {
            InputStreamReader reader = new InputStreamReader(input, encoding);
            String result = CharStreams.toString(reader);
            reader.close();
            return result;
        } catch (IOException ex) {
            throw new StringConvertionException(ex);
        }
    }
    
    public static String URLEncode(String string, String encoding) throws StringConvertionException{
        try {
            return URLEncoder.encode(string, encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new StringConvertionException(ex);
        }
    }
    
    public static String URLDecode(String string, String encoding) throws StringConvertionException{
        try {
            return URLDecoder.decode(string, encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new StringConvertionException(ex);
        }
    }    
}
