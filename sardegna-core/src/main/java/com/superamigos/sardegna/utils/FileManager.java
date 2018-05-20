/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.spark.api.java.JavaSparkContext;

/**
 *
 * @author fably
 */
public abstract class FileManager {
    
    private int numberOfPartitions;
    
    public abstract void write(String path, List<String> toWrite);
    
    public int getNumberOfPartitions(){
        return numberOfPartitions;
    }
    
    public void setNumberOfPartitions(int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }

    /*InputStream openR(String fileName) throws FileNotFoundException;
    OutputStream openW(String fileName, boolean append) throws FileNotFoundException;
    void close(OutputStream os) throws IOException;
    boolean exists(String fileName);
    boolean isDirectory(String fileName);
    boolean delete(String fileName);
    boolean mkdirs(String fileName);*/
}
