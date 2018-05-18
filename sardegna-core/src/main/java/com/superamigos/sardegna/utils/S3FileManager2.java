/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.utils;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.out;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fably
 
public class S3FileManager2 implements FileManager {

    private AmazonS3 s3Client;

    public S3FileManager2() {
        s3Client = new AmazonS3Client(new ProfileCredentialsProvider());
    }

    @Override
    public InputStream openR(String hdfsPath, String fileName) throws FileNotFoundException {
        S3Object object = s3Client.getObject(new GetObjectRequest(fileName, fileName));
        InputStream objectData = object.getObjectContent();

        return objectData;
    }

    @Override
    public OutputStream openW(String hdfsPath, String fileName, boolean append) throws FileNotFoundException {
        Bucket b = s3Client.createBucket(new CreateBucketRequest(fileName));
        S3Object object = s3Client.getObject(
                new GetObjectRequest(fileName, fileName));
        InputStream in = object.getObjectContent();
        object.

        in.close();
        out.close();
    }

    @Override
    public void close(String hdfsPath, OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean exists(String hdfsPath, String fileName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDirectory(String hdfsPath, String fileName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delete(String hdfsPath, String fileName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean mkdirs(String hdfsPath, String fileName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
*/