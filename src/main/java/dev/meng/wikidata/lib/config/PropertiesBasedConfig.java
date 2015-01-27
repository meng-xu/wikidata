/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.lib.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *
 * @author xumeng
 */
public class PropertiesBasedConfig {

    private Properties properties;
    
    public PropertiesBasedConfig(String filepath) throws ConfigException {
            
        try {
            properties = new Properties();
            properties.load(new FileInputStream(filepath));

            for(Field field : this.getClass().getDeclaredFields()){
                String value = properties.getProperty(field.getName());
                if(field.getType()==String.class){
                    field.set(this, properties.get(field.getName()));
                } else if(field.getType()==int.class){
                    field.set(this, Integer.parseInt(value));
                } else if(field.getType()==double.class){
                    field.set(this, Double.parseDouble(value));
                } else if(field.getType()==boolean.class){
                    field.set(this, Boolean.parseBoolean(value));
                } else if(field.getType()==long.class){
                    field.set(this, Long.parseLong(value));
                } else if(field.getType()==String[].class){
                    field.set(this, parse(value));
                } else if(field.getType()==int[].class){
                    String[] result = parse(value);
                    int[] values = new int[result.length];
                    for(int i=0;i<values.length;i++){
                        values[i] = Integer.parseInt(result[i]);
                    }
                    field.set(this, values);
                } else if(field.getType()==double[].class){
                    String[] result = parse(value);
                    double[] values = new double[result.length];
                    for(int i=0;i<values.length;i++){
                        values[i] = Double.parseDouble(result[i]);
                    }
                    field.set(this, values);
                } else if(field.getType()==boolean[].class){
                    String[] result = parse(value);
                    boolean[] values = new boolean[result.length];
                    for(int i=0;i<values.length;i++){
                        values[i] = Boolean.parseBoolean(result[i]);
                    }
                    field.set(this, values);
                } else if(field.getType()==long[].class){
                    String[] result = parse(value);
                    long[] values = new long[result.length];
                    for(int i=0;i<values.length;i++){
                        values[i] = Long.parseLong(result[i]);
                    }
                    field.set(this, values);
                }
            }
        } catch (FileNotFoundException ex) {
            throw new ConfigException("Config file "+filepath+" not found.", ex);
        } catch (IOException ex) {
            throw new ConfigException("Error in reading config file "+filepath+".", ex);
        } catch (NumberFormatException ex){
            throw new ConfigException("Error in config file "+filepath+".", ex);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new ConfigException("Error in config declaration class "+this.getClass()+".", ex);
        }
    }
    
    private String[] parse(String string){
        StringTokenizer tokenizer = new StringTokenizer(string, ConfigConstant.DELIMITER);
        String[] result = new String[tokenizer.countTokens()];
        for(int i=0;i<result.length;i++){
            result[i] = tokenizer.nextToken();
        }
        return result;
    }
}
