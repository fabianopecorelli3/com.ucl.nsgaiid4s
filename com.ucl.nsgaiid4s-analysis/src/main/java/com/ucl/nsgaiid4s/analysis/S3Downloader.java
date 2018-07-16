package com.ucl.nsgaiid4s.analysis;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class S3Downloader {

    public static void main(String[] args) throws IOException, InterruptedException {
        String clientRegion = "us-west-2";
        String bucketName = "moga-spark";
        int run = Integer.parseInt(args[0]); //#run
        String baseDir = args[1];
        String outputDir = args[2];
        String conf[] = {"A2", "A3", "A4", "A6", "A7", "A8", "A10", "A11", "A12", "B2", "B3", "B4", "B6", "B7", "B8", "B10", "B11", "B12", "C2", "C3", "C4", "C6", "C7", "C8", "C10", "C11", "C12"};
        //String conf[] = {"C10"};
        String policies[] = {"DR", "REM", "REPL", "REPR", "RR"};
        String problems[] = {"ZDT1"/*, "ZDT2", "ZDT3", "ZDT4", "ZDT6"*/};

        int totaleFiles = (conf.length * policies.length * problems.length * ((run/* * 2*/) + 1));
        System.out.println("[INFO] Mancano "+totaleFiles+" files");

        prepareOutputDirectory(outputDir, conf, policies, problems);

        S3Object tempFUN = null;
        S3Object objectPortion = null, headerOverrideObject = null;
        S3Object tempVAR = null;
        S3Object tempTIMES = null;
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new ProfileCredentialsProvider())
                    .build();
            for (int i = 0; i < conf.length; i++) {
                System.out.println("Downloading an objects for: " + conf[i]);
                for (int h = 0; h < policies.length; h++) {
                    for (int j = 0; j < problems.length; j++) {
                        for (int z = 0; z < run; z++) {
                            
                            String fullPathFUN = baseDir + "/" + conf[i] + "/NSGAStudy/data/" + policies[h] + "/" + problems[j] + "/FUN" + z + ".tsv/part-00000";
                            String fullPathVAR = baseDir + "/" + conf[i] + "/NSGAStudy/data/" + policies[h] + "/" + problems[j] + "/VAR" + z + ".tsv/part-00000";

                            System.out.println("Downloading FUN" + z + " for the " + problems[j] + " problem and the " + policies[h] + " policy ["+conf[i]+"]");
                            tempFUN = s3Client.getObject(new GetObjectRequest(bucketName, fullPathFUN));
                            System.out.println("Downloading VAR" + z + " for the " + problems[j] + " problem and the " + policies[h] + " policy ["+conf[i]+"]");
                            tempVAR = s3Client.getObject(new GetObjectRequest(bucketName, fullPathVAR));

                           
                            saveFile(true, tempFUN.getObjectContent(), outputDir + "/" + conf[i] + "/" + policies[h] + "/" + problems[j] + "/FUN" + z + ".tsv");
                            saveFile(true, tempVAR.getObjectContent(), outputDir + "/" + conf[i] + "/" + policies[h] + "/" + problems[j] + "/VAR" + z + ".tsv");
                            //totaleFiles = totaleFiles-2;
                            totaleFiles = totaleFiles--;
                        }
                        
                            System.out.println("Downloading TIMES for the " + problems[j] + " problem and the " + policies[h] + " policy ["+conf[i]+"]");
                        String fullPathTIMES = baseDir + "/" + conf[i] + "/NSGAStudy/data/" + policies[h] + "/" + problems[j] + "/TIMES/" + "part-00000";
                        tempTIMES = s3Client.getObject(new GetObjectRequest(bucketName, fullPathTIMES));
                        
                        saveFile(false, tempTIMES.getObjectContent(), outputDir + "/" + conf[i] + "/" + policies[h] + "/" + problems[j] + "/TIMES");
                        totaleFiles--;

                        System.out.println("[INFO] Mancano "+totaleFiles+" files");
                    }

                }

                /*
                ResponseHeaderOverrides headerOverrides = new ResponseHeaderOverrides()
                        .withCacheControl("No-cache")
                        .withContentDisposition("attachment; filename=example.txt");
                GetObjectRequest getObjectRequestHeaderOverride = new GetObjectRequest(bucketName, conf[i])
                        .withResponseHeaders(headerOverrides);
                headerOverrideObject = s3Client.getObject(getObjectRequestHeaderOverride);
                 */
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (tempFUN != null) {
                tempFUN.close();
            }

            if (tempVAR != null) {
                tempVAR.close();
            }

            if (tempTIMES != null) {
                tempTIMES.close();
            }
          
            if (objectPortion != null) {
                objectPortion.close();
            }
            if (headerOverrideObject != null) {
                headerOverrideObject.close();
            }
        }

    }

    private static void displayTextInputStream(InputStream input) throws IOException {
        // Read the text input stream one line at a time and display each line.
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = null;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println();
    }

    private static void saveFile(boolean flag, InputStream input, String outDir) throws IOException {

        // Read the text input stream one line at a time and display each line.
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        char[] line = new char[1024];
        BufferedWriter bw = new BufferedWriter(new FileWriter(outDir));
        String toWrite = "";
        int r;
        while ((r = reader.read(line)) != -1) {
            for (int i = 0; i < r; i++) {
                toWrite += line[i];
            }
            //System.out.println(toWrite);

        }

        if (flag) {
            toWrite = toWrite.replace("\t\n", "\t");
        }
        toWrite = toWrite.replace("\n\n", "\n");
        bw.write(toWrite);
        bw.close();
        //System.out.println();
    }

    private static void prepareOutputDirectory(String basedir, String conf[], String policies[], String problems[]) {
        for (int i = 0; i < conf.length; i++) {
            System.out.println("Downloading an objects for: " + conf[i]);
            for (int h = 0; h < policies.length; h++) {
                for (int j = 0; j < problems.length; j++) {
                    File file = new File(basedir + "/" + conf[i] + "/" + policies[h] + "/" + problems[j] + "/");

                    if (!file.exists()) {
                        file.mkdirs();
                    }
                }
            }
        }
    }

}
