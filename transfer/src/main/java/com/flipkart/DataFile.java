package com.flipkart;

/**
 * Created on 03/03/17 by dark magic.
 */
public interface DataFile {
    public void getFile(String toFilePath) throws Exception;
    //TODO on a different interface
    public void writeFile(String toFilePath) throws Exception;
}
