package com.superamigos.sardegna.utils;

import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.fileoutput.FileOutputContext;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Class using the default method for getting a buffered writer
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class  DefaultFileOutputContext implements FileOutputContext {
  private static final String DEFAULT_SEPARATOR = " " ;

  protected String fileName;
  protected String separator;
  private FileManager fileManager;
  private String hdfsPath;

  public DefaultFileOutputContext(String fileName, FileManager fileManager, String hdfsPath) {
    this.fileName = fileName ;
    this.separator = DEFAULT_SEPARATOR ;
    this.fileManager = fileManager;
    this.hdfsPath = hdfsPath;
  }

  @Override
  public BufferedWriter getFileWriter() {
    OutputStream outputStream ;
    try {
      outputStream = fileManager.openW(hdfsPath, fileName, false);
    } catch (FileNotFoundException e) {
      throw new JMetalException("Exception when calling method getFileWriter()", e) ;
    }
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

    return new BufferedWriter(outputStreamWriter);
  }

  @Override
  public String getSeparator() {
    return separator;
  }

  @Override
  public void setSeparator(String separator) {
    this.separator = separator;
  }

  @Override
  public String getFileName() {
    return fileName ;
  }
}
