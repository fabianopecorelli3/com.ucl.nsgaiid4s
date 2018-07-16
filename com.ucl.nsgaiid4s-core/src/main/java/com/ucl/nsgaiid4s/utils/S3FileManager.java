/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucl.nsgaiid4s.utils;

import java.util.List;
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
    
}
