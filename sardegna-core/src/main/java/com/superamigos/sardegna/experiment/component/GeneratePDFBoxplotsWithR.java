package com.superamigos.sardegna.experiment.component;

import com.superamigos.sardegna.utils.FileManager;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class generates a R script that generates an eps file containing
 * boxplots
 *
 * The results are a set of R files that are written in the directory
 * {@link Experiment #getExperimentBaseDirectory()}/R. Each file is called as
 * indicatorName.Wilcoxon.R
 *
 * To run the R script: Rscript indicatorName.Wilcoxon.R To generate the
 * resulting Latex file: pdflatex indicatorName.Wilcoxon.tex
 *
 */
public class GeneratePDFBoxplotsWithR<Result> implements ExperimentComponent {

    private static final String DEFAULT_R_DIRECTORY = "R";

    private final Experiment<?, Result> experiment;
    private int numberOfRows;
    private int numberOfColumns;
    private boolean displayNotch;
    private FileManager fileManager;
    private String hdfsPath;

    public GeneratePDFBoxplotsWithR(Experiment<?, Result> experimentConfiguration, FileManager fileManager, String hdfsPath) {
        this.experiment = experimentConfiguration;

        displayNotch = false;
        numberOfRows = 1;
        numberOfColumns = 1;

        experiment.removeDuplicatedAlgorithms();
        this.fileManager = fileManager;
        this.hdfsPath = hdfsPath;
    }

    public GeneratePDFBoxplotsWithR<Result> setRows(int rows) {
        numberOfRows = rows;

        return this;
    }

    public GeneratePDFBoxplotsWithR<Result> setColumns(int columns) {
        numberOfColumns = columns;

        return this;
    }

    public GeneratePDFBoxplotsWithR<Result> setDisplayNotch() {
        displayNotch = true;

        return this;
    }

    @Override
    public void run() throws IOException {
        String rDirectoryName = experiment.getExperimentBaseDirectory() + "/" + DEFAULT_R_DIRECTORY;

        String finalPath = experiment.getExperimentBaseDirectory().replaceAll("\\\\", "/");
        if (!fileManager.exists(hdfsPath, rDirectoryName)) {
            fileManager.mkdirs(hdfsPath, rDirectoryName);
            System.out.println("Creating " + rDirectoryName + " directory");
        }
        for (GenericIndicator<? extends Solution<?>> indicator : experiment.getIndicatorList()) {
            String rFileName = rDirectoryName + "/" + indicator.getName() + ".Boxplot" + ".R";
            OutputStream os = fileManager.openW(hdfsPath, rFileName, false);
            os.write(("pdf(\""
                    + finalPath
                    + "/R/"
                    + indicator.getName()
                    + ".Boxplot.pdf\")"
                    + "\n").getBytes("UTF-8"));

            os.write(("resultDirectory<-\""
                    + finalPath
                    + "/data" + "\"" + "\n").getBytes("UTF-8"));
            os.write(("qIndicator <- function(indicator, problem)" + "\n").getBytes("UTF-8"));
            os.write(("{" + "\n").getBytes("UTF-8"));

            for (int i = 0; i < experiment.getAlgorithmList().size(); i++) {
                String algorithmName = experiment.getAlgorithmList().get(i).getAlgorithmTag();
                os.write(("file" + algorithmName + "<-paste(resultDirectory, \"" + algorithmName + "\", sep=\"/\")" + "\n").getBytes("UTF-8"));
                os.write(("file" + algorithmName + "<-paste(file" + algorithmName + ", " + "problem, sep=\"/\")" + "\n").getBytes("UTF-8"));
                os.write(("file" + algorithmName + "<-paste(file" + algorithmName + ", " + "indicator, sep=\"/\")" + "\n").getBytes("UTF-8"));
                os.write((algorithmName + "<-scan(" + "file" + algorithmName + ")" + "\n").getBytes("UTF-8"));
                os.write(("\n").getBytes("UTF-8"));
            }

            os.write(("algs<-c(").getBytes("UTF-8"));
            for (int i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(("\"" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "\",").getBytes("UTF-8"));
            } // for
            os.write(("\"" + experiment.getAlgorithmList().get(experiment.getAlgorithmList().size() - 1).getAlgorithmTag() + "\")" + "\n").getBytes("UTF-8"));

            os.write(("boxplot(").getBytes("UTF-8"));
            for (int i = 0; i < experiment.getAlgorithmList().size(); i++) {
                os.write((experiment.getAlgorithmList().get(i).getAlgorithmTag() + ",").getBytes("UTF-8"));
            } // for
            if (displayNotch) {
                os.write(("names=algs, notch = TRUE)" + "\n").getBytes("UTF-8"));
            } else {
                os.write(("names=algs, notch = FALSE)" + "\n").getBytes("UTF-8"));
            }
            os.write(("titulo <-paste(indicator, problem, sep=\":\")" + "\n").getBytes("UTF-8"));
            os.write(("title(main=titulo)" + "\n").getBytes("UTF-8"));

            os.write(("}" + "\n").getBytes("UTF-8"));

            os.write(("par(mfrow=c(" + numberOfRows + "," + numberOfColumns + "))" + "\n").getBytes("UTF-8"));

            os.write(("indicator<-\"" + indicator.getName() + "\"" + "\n").getBytes("UTF-8"));

            for (ExperimentProblem<?> problem : experiment.getProblemList()) {
                os.write(("qIndicator(indicator, \"" + problem.getTag() + "\")" + "\n").getBytes("UTF-8"));
            }

            fileManager.close(hdfsPath, os);

            //execute R script on Windows --> You need to set ENVIRONMENT VARIABLE on the R path (es. C:\Program Files\R\R-3.5.0\bin) 
            runRScript(rFileName);

        }

        /*
        Create TIMES.Boxplot.R
         */
        createTimesBoxplot();
    }

    public void runRScript(String fileRPath) {
        try {
            Runtime.getRuntime().exec("Rscript " + fileRPath);
        } catch (IOException ex) {
            Logger.getLogger(GeneratePDFBoxplotsWithR.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createTimesBoxplot() throws IOException {
        String rDirectoryName = experiment.getExperimentBaseDirectory() + "/" + DEFAULT_R_DIRECTORY;
        File rOutput;
        String finalPath = experiment.getExperimentBaseDirectory().replaceAll("\\\\", "/");

        String rFileName = rDirectoryName + "/TIMES.Boxplot" + ".R";
        FileWriter os = new FileWriter(rFileName, false);
        os.write("pdf(\""
                + finalPath
                + "/R/"
                + "TIMES"
                + ".Boxplot.pdf\")"
                + "\n");

        os.write("resultDirectory<-\""
                + finalPath
                + "/data" + "\"" + "\n");
        os.write("qIndicator <- function(indicator, problem)" + "\n");
        os.write("{" + "\n");

        for (int i = 0; i < experiment.getAlgorithmList().size(); i++) {
            String algorithmName = experiment.getAlgorithmList().get(i).getAlgorithmTag();
            os.write("file" + algorithmName + "<-paste(resultDirectory, \"" + algorithmName + "\", sep=\"/\")" + "\n");
            os.write("file" + algorithmName + "<-paste(file" + algorithmName + ", " + "problem, sep=\"/\")" + "\n");
            os.write("file" + algorithmName + "<-paste(file" + algorithmName + ", " + "indicator, sep=\"/\")" + "\n");
            os.write(algorithmName + "<-scan(" + "file" + algorithmName + ")" + "\n");
            os.write("\n");
        }

        os.write("algs<-c(");
        for (int i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
            os.write("\"" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "\",");
        } // for
        os.write("\"" + experiment.getAlgorithmList().get(experiment.getAlgorithmList().size() - 1).getAlgorithmTag() + "\")" + "\n");

        os.write("boxplot(");
        for (int i = 0; i < experiment.getAlgorithmList().size(); i++) {
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag() + ",");
        } // for
        if (displayNotch) {
            os.write("names=algs, notch = TRUE)" + "\n");
        } else {
            os.write("names=algs, notch = FALSE)" + "\n");
        }
        os.write("titulo <-paste(indicator, problem, sep=\":\")" + "\n");
        os.write("title(main=titulo)" + "\n");

        os.write("}" + "\n");

        os.write("par(mfrow=c(" + numberOfRows + "," + numberOfColumns + "))" + "\n");

        os.write("indicator<-\"" + "TIMES" + "\"" + "\n");

        for (ExperimentProblem<?> problem : experiment.getProblemList()) {
            os.write("qIndicator(indicator, \"" + problem.getTag() + "\")" + "\n");
        }

        os.close();

        //execute R script on Windows --> You need to set ENVIRONMENT VARIABLE on the R path (es. C:\Program Files\R\R-3.5.0\bin) 
        runRScript(rFileName);
    }
}
