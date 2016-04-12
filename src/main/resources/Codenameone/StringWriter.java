/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pmovil.jsonapi;

import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author fabricio
 */
public class StringWriter extends Writer {

    private StringBuffer internal = new StringBuffer();
    
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        internal.append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        // nothing to do
    }

    @Override
    public void close() throws IOException {
        // nothing to do
    }

    @Override
    public String toString() {
        return internal.toString();
    }
    
}
