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
public class StringConvertionException extends Exception {

    /**
     * Creates a new instance of <code>CodecException</code> without detail
     * message.
     */
    public StringConvertionException() {
    }

    /**
     * Constructs an instance of <code>CodecException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public StringConvertionException(String msg) {
        super(msg);
    }
    
    public StringConvertionException(Throwable ex) {
        super(ex);
    }    
}
