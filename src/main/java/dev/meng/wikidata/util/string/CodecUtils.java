/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.util.string;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 *
 * @author meng
 */
public class CodecUtils {

    public static final String ASCII_TO_UNI_BINARY_PATH = "bin/util/ascii2uni";

    public static String asciiToUnicode(String string, AsciiToUnicodeFormat format) throws StringConvertionException {
        try {
            ProcessBuilder builder = new ProcessBuilder(ASCII_TO_UNI_BINARY_PATH, "-a", format.getOption());
            Process process = builder.start();
            
            process.getInputStream();
            
            OutputStream stdin = process.getOutputStream();
            InputStream stdout = process.getInputStream();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
            
            writer.write(string);
            writer.newLine();
            writer.flush();
            writer.close();
            
            String result = "";
            String line;
            while ((line = reader.readLine()) != null) {
                result = result + line;
            }
            
            process.destroy();
            
            return result;
        } catch (IOException ex) {
            throw new StringConvertionException(ex);
        }
    }
}
