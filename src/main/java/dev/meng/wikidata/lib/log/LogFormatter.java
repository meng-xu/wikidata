/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.meng.wikidata.lib.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author meng
 */
public class LogFormatter extends Formatter{

    @Override
    public String format(LogRecord record) {
        StringBuilder string = new StringBuilder();
        string.append(new Date(record.getMillis()))
                .append(" - ")
                .append(formatMessage(record))
                .append("\n");

        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            pw.close();
            string.append(sw.toString());
        }

        return string.toString();
    }
}
