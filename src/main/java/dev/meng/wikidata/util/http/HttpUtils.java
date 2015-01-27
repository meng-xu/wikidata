/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.util.http;

import dev.meng.wikidata.Configure;
import dev.meng.wikidata.util.string.StringConvertionException;
import dev.meng.wikidata.util.string.StringUtils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import org.json.JSONObject;

/**
 *
 * @author meng
 */
public class HttpUtils {
    
    public static JSONObject queryForJSONResponse(URL url, String encoding) throws ProtocolException, IOException, StringConvertionException {
        JSONObject response = null;
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        
        if (connection.getResponseCode() == 200) {
            response = new JSONObject(StringUtils.inputStreamToString(connection.getInputStream(), encoding));
        } else {
            throw new IOException("Error in opening: " + url + ", " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
        
        return response;
    }    
}
