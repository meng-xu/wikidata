/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.lib.log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author meng
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
    LogOutput output();
}
