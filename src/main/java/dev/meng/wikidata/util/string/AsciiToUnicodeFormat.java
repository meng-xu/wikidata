/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.util.string;

/**
 *
 * @author meng
 */
public enum AsciiToUnicodeFormat {
    PERCENTAGE_HEX("J"),
    SLASH_X_HEX("7");
    
    private String option;
    
    private AsciiToUnicodeFormat(String option){
        this.option = option;
    }
    
    public String getOption(){
        return option;
    }
}
