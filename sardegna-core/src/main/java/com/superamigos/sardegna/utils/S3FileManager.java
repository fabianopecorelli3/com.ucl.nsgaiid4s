/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.utils;

import static com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.URI;
import java.io.File;
import java.io.FileNotFoundException;
import org.apache.hadoop.fs.FileSystem;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Progressable;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 *
 * @author fably
 */
public class S3FileManager extends FileManager {

    
    @Override
    public void write(String path, List<String> toWrite) {
        SparkConf sparkConf = new SparkConf().setAppName("Sardegna");
        JavaSparkContext sparkContext = new JavaSparkContext(SparkContext.getOrCreate(sparkConf));
        JavaRDD<String> rdd = sparkContext.parallelize(toWrite, getNumberOfPartitions());
        rdd.coalesce(1).saveAsTextFile("s3a://AKIAJA5KUA3KTLB4JN3A:Vn3EDMEaDqyePYxRFihCN7dkDyTJpgzsNiOdwJoW@moga-spark/"+path);
    }
/*
    @Override
    public InputStream openR(String fileName) throws FileNotFoundException {
        Configuration configuration = new Configuration();
        FileSystem hdfs;
        InputStream is = null;
        try {
            hdfs = FileSystem.get(new URI(hdfsPath), configuration);
            Path file = new Path(hdfsPath + "/" + fileName);
            if (hdfs.exists(file)) {
                is = hdfs.open(file);
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return is;
    }

    @Override
    public OutputStream openW(String fileName, boolean append) throws FileNotFoundException {
        Configuration configuration = new Configuration();
        FileSystem hdfs;
        OutputStream os = null;
        try {
            hdfs = FileSystem.get(new URI(hdfsPath), configuration);
            Path file = new Path(hdfsPath + "/" + fileName);
            if (hdfs.exists(file)) {
                hdfs.delete(file, true);
            }
            os = (append) ? hdfs.append(file)
                    : hdfs.create(file);
        } catch (URISyntaxException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return os;
    }

    @Override
    public void close(OutputStream os) throws IOException {
        Configuration configuration = new Configuration();
        try {
            FileSystem hdfs = FileSystem.get(new URI(hdfsPath), configuration);
            hdfs.close();
        } catch (URISyntaxException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        os.close();
    }

    @Override
    public boolean exists(String fileName) {
        Configuration configuration = new Configuration();
        FileSystem hdfs;
        OutputStream os = null;
        try {
            hdfs = FileSystem.get(new URI(hdfsPath), configuration);
            Path file = new Path(hdfsPath + "/" + fileName);
            return hdfs.exists(file);
        } catch (URISyntaxException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean isDirectory(String fileName) {
        /*Configuration configuration = new Configuration();
        FileSystem hdfs;
        OutputStream os = null;
        try {
            hdfs = FileSystem.get(new URI(hdfsPath), configuration);
            Path file = new Path(hdfsPath + "/" + fileName);
            return hdfs.isDirectory(file);
        } catch (URISyntaxException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public boolean delete(String fileName) {
        Configuration configuration = new Configuration();
        FileSystem hdfs;
        OutputStream os = null;
        try {
            hdfs = FileSystem.get(new URI(hdfsPath), configuration);
            Path file = new Path(hdfsPath + "/" + fileName);
            return hdfs.delete(file, false);
        } catch (URISyntaxException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean mkdirs(String fileName) {
        /*Configuration configuration = new Configuration();
        FileSystem hdfs;
        OutputStream os = null;
        try {
            hdfs = FileSystem.get(new URI(hdfsPath), configuration);
            Path file = new Path(hdfsPath + "/" + fileName);
            return hdfs.mkdirs(file);
        } catch (URISyntaxException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(S3FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
*/
}
