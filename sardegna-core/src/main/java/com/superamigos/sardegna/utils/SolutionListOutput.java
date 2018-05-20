package com.superamigos.sardegna.utils;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.util.fileoutput.FileOutputContext;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SolutionListOutput {

    private List<? extends Solution<?>> solutionList;
    private List<Boolean> isObjectiveToBeMinimized;
    private FileManager fileManager;
    private String funFile;
    private String varFile;
    
    public SolutionListOutput(List<? extends Solution<?>> solutionList, FileManager fileManager) {
        this.solutionList = solutionList;
        this.isObjectiveToBeMinimized = null;
        this.fileManager = fileManager;
    }

    public void setFunFile(String funFile) {
        this.funFile = funFile;
    }

    public void setVarFile(String varFile) {
        this.varFile = varFile;
    }
    
    public SolutionListOutput setObjectiveMinimizingObjectiveList(List<Boolean> isObjectiveToBeMinimized) {
        this.isObjectiveToBeMinimized = isObjectiveToBeMinimized;

        return this;
    }

    public void print() {
        if (isObjectiveToBeMinimized == null) {
            printObjectivesToFile(solutionList);
        } else {
            printObjectivesToFile(solutionList, isObjectiveToBeMinimized);
        }
        printVariablesToFile(solutionList);
    }

    public void printVariablesToFile(List<? extends Solution<?>> solutionList) {
        List<String> toPrint = new ArrayList<>();

        if (solutionList.size() > 0) {
            int numberOfVariables = solutionList.get(0).getNumberOfVariables();
            for (int i = 0; i < solutionList.size(); i++) {
                for (int j = 0; j < numberOfVariables; j++) {
                    toPrint.add(solutionList.get(i).getVariableValueString(j) + "\t");
                }
                toPrint.add("\n");
            }
            fileManager.write(varFile, toPrint);
        }

    }

    public void printObjectivesToFile(List<? extends Solution<?>> solutionList) {
        List<String> toPrint = new ArrayList<>();

        if (solutionList.size() > 0) {
            int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
            for (int i = 0; i < solutionList.size(); i++) {
                for (int j = 0; j < numberOfObjectives; j++) {
                    toPrint.add(solutionList.get(i).getObjective(j) + "\t");
                }
                toPrint.add("\n");
            }
            fileManager.write(funFile, toPrint);
        }
    }

    public void printObjectivesToFile(List<? extends Solution<?>> solutionList, List<Boolean> minimizeObjective) {
        List<String> toPrint = new ArrayList<>();

        if (solutionList.size() > 0) {
            int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
            if (numberOfObjectives != minimizeObjective.size()) {
                throw new JMetalException("The size of list minimizeObjective is not correct: " + minimizeObjective.size());
            }
            for (int i = 0; i < solutionList.size(); i++) {
                for (int j = 0; j < numberOfObjectives; j++) {
                    if (minimizeObjective.get(j)) {
                        toPrint.add(solutionList.get(i).getObjective(j) + "\t");
                    } else {
                        toPrint.add(-1.0 * solutionList.get(i).getObjective(j) + "\t");
                    }
                }
                toPrint.add("\n");
            }
            fileManager.write(funFile, toPrint);
        }
    }
}
