package com.superamigos.sardegna.utils;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import org.uma.jmetal.util.fileoutput.FileOutputContext;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SolutionListOutput {
  private FileOutputContext varFileContext;
  private FileOutputContext funFileContext;
  private String varFileName = "VAR";
  private String funFileName = "FUN";
  private String separator = "\t";
  private List<? extends Solution<?>> solutionList;
  private List<Boolean> isObjectiveToBeMinimized ;
  private FileManager fileManager;
  private String hdfsPath;

  public SolutionListOutput(List<? extends Solution<?>> solutionList, FileManager fileManager, String hdfsPath) {
    varFileContext = new DefaultFileOutputContext(varFileName, fileManager, hdfsPath);
    funFileContext = new DefaultFileOutputContext(funFileName, fileManager, hdfsPath);
    varFileContext.setSeparator(separator);
    funFileContext.setSeparator(separator);
    this.solutionList = solutionList;
    isObjectiveToBeMinimized = null ;
    this.fileManager = fileManager;
    this.hdfsPath = hdfsPath;
  }

  public SolutionListOutput setVarFileOutputContext(FileOutputContext fileContext) {
    varFileContext = fileContext;

    return this;
  }

  public SolutionListOutput setFunFileOutputContext(FileOutputContext fileContext) {
    funFileContext = fileContext;

    return this;
  }

  public SolutionListOutput setObjectiveMinimizingObjectiveList(List<Boolean> isObjectiveToBeMinimized) {
    this.isObjectiveToBeMinimized = isObjectiveToBeMinimized ;

    return this;
  }

  public SolutionListOutput setSeparator(String separator) {
    this.separator = separator;
    varFileContext.setSeparator(this.separator);
    funFileContext.setSeparator(this.separator);

    return this;
  }

  public void print() {
    if (isObjectiveToBeMinimized == null) {
      printObjectivesToFile(funFileContext, solutionList);
    } else {
      printObjectivesToFile(funFileContext, solutionList, isObjectiveToBeMinimized);
    }
    printVariablesToFile(varFileContext, solutionList);
  }

  public void printVariablesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList) {
    BufferedWriter bufferedWriter = context.getFileWriter();

    try {
      if (solutionList.size() > 0) {
        int numberOfVariables = solutionList.get(0).getNumberOfVariables();
        for (int i = 0; i < solutionList.size(); i++) {
          for (int j = 0; j < numberOfVariables; j++) {
            bufferedWriter.write(solutionList.get(i).getVariableValueString(j) + context.getSeparator());
          }
          bufferedWriter.newLine();
        }
      }

      bufferedWriter.close();
    } catch (IOException e) {
      throw new JMetalException("Error writing data ", e) ;
    }

  }

  public void printObjectivesToFile(FileOutputContext context, List<? extends Solution<?>> solutionList) {
    BufferedWriter bufferedWriter = context.getFileWriter();

    try {
      if (solutionList.size() > 0) {
        int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
        for (int i = 0; i < solutionList.size(); i++) {
          for (int j = 0; j < numberOfObjectives; j++) {
            bufferedWriter.write(solutionList.get(i).getObjective(j) + context.getSeparator());
          }
          bufferedWriter.newLine();
        }
      }

      bufferedWriter.close();
    } catch (IOException e) {
      throw new JMetalException("Error printing objecives to file: ", e);
    }
  }

  public void printObjectivesToFile(FileOutputContext context,
                                    List<? extends Solution<?>> solutionList,
                                    List<Boolean> minimizeObjective) {
    BufferedWriter bufferedWriter = context.getFileWriter();

    try {
      if (solutionList.size() > 0) {
        int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
        if (numberOfObjectives != minimizeObjective.size()) {
          throw new JMetalException("The size of list minimizeObjective is not correct: " + minimizeObjective.size()) ;
        }
        for (int i = 0; i < solutionList.size(); i++) {
          for (int j = 0; j < numberOfObjectives; j++) {
            if (minimizeObjective.get(j)) {
              bufferedWriter.write(solutionList.get(i).getObjective(j) + context.getSeparator());
            } else {
              bufferedWriter.write(-1.0 * solutionList.get(i).getObjective(j) + context.getSeparator());
            }
          }
          bufferedWriter.newLine();
        }
      }

      bufferedWriter.close();
    } catch (IOException e) {
      throw new JMetalException("Error printing objecives to file: ", e);
    }
  }

  /*
   * Wrappers for printing with default configuration
   */
  public void printObjectivesToFile(String fileName) throws IOException {
    printObjectivesToFile(new DefaultFileOutputContext(fileName,fileManager, hdfsPath), solutionList);
  }

  public void printObjectivesToFile(String fileName, List<Boolean> minimizeObjective) throws IOException {
    printObjectivesToFile(new DefaultFileOutputContext(fileName,fileManager, hdfsPath), solutionList, minimizeObjective);
  }

  public void printVariablesToFile(String fileName) throws IOException {
    printVariablesToFile(new DefaultFileOutputContext(fileName,fileManager, hdfsPath), solutionList);
  }

}
