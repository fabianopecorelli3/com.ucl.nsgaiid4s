/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucl.nsgaiid4s.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.spark.api.java.JavaSparkContext;

/**
 *
 * @author fably
 */
public class LocalFileManger extends FileManager {
    
    @Override
    public void write(String path, List<String> toWrite) {
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(path));
            for (Object o : toWrite) {
                bw.write(o.toString());
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(LocalFileManger.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /*@Override
    public InputStream openR(String fileName) throws FileNotFoundException {
        return new FileInputStream(fileName);
    }

    @Override
    public OutputStream openW(String fileName, boolean append) throws FileNotFoundException {
        return new FileOutputStream(fileName, append);
    }

    @Override
    public void close(OutputStream os) throws IOException {
        os.close();
    }

    @Override
    public boolean exists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    @Override
    public boolean isDirectory(String fileName) {
        File file = new File(fileName);
        return file.isDirectory();
    }

    @Override
    public boolean delete(String fileName) {
        File file = new File(fileName);
        return file.delete();
    }

    @Override
    public boolean mkdirs(String fileName) {
        File file = new File(fileName);
        return file.mkdirs();
    }
     */
}
