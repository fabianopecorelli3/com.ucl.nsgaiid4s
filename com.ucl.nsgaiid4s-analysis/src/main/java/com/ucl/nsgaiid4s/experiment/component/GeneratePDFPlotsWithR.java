package com.ucl.nsgaiid4s.experiment.component;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentComponent;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class GeneratePDFPlotsWithR<Result> implements ExperimentComponent {

    private static final String DEFAULT_R_DIRECTORY = "R";

    private final Experiment<?, Result> experiment;
    private int numberOfRows;
    private int numberOfColumns;
    private boolean displayNotch;

    public GeneratePDFPlotsWithR(Experiment<?, Result> experimentConfiguration) {
        this.experiment = experimentConfiguration;

        displayNotch = false;
        numberOfRows = 1;
        numberOfColumns = 1;

        experiment.removeDuplicatedAlgorithms();
    }

    public GeneratePDFPlotsWithR<Result> setRows(int rows) {
        numberOfRows = rows;

        return this;
    }

    public GeneratePDFPlotsWithR<Result> setColumns(int columns) {
        numberOfColumns = columns;

        return this;
    }

    public GeneratePDFPlotsWithR<Result> setDisplayNotch() {
        displayNotch = true;

        return this;
    }

    @Override
    public void run() throws IOException {
        String rDirectoryName = experiment.getExperimentBaseDirectory() + "/" + DEFAULT_R_DIRECTORY;
        File rOutput;

        String finalPath = experiment.getExperimentBaseDirectory().replaceAll("\\\\", "/");
        rOutput = new File(rDirectoryName);
        if (!rOutput.exists()) {
            new File(rDirectoryName).mkdirs();
            System.out.println("Creating " + rDirectoryName + " directory");
        }
        for (GenericIndicator<? extends Solution<?>> indicator : experiment.getIndicatorList()) {
            String rFileName = rDirectoryName + "/" + indicator.getName() + ".Plot" + ".R";
            FileWriter os = new FileWriter(rFileName, false);
            os.write("pdf(\""
                    + finalPath
                    + "/R/"
                    + indicator.getName()
                    + ".Plot.pdf\")"
                    + "\n");

            os.write("resultDirectory<-\""
                    + finalPath
                    + "/data" + "\"" + "\n");
            os.write("qIndicator <- function(indicator, problems)" + "\n");
            os.write("{" + "\n");

            os.write("for(problem in problems){\n");
            for (int i = 0; i < experiment.getAlgorithmList().size(); i++) {
                String algorithmName = experiment.getAlgorithmList().get(i).getAlgorithmTag();
                os.write("file" + algorithmName + "<-paste(resultDirectory, \"" + algorithmName + "\", sep=\"/\")" + "\n");
                os.write("file" + algorithmName + "<-paste(file" + algorithmName + ", " + "problem, sep=\"/\")" + "\n");
                os.write("file" + algorithmName + "<-paste(file" + algorithmName + ", " + "indicator, sep=\"/\")" + "\n");
                os.write("if (exists(\"" + algorithmName + "_mean\") && exists(\"" + algorithmName + "_median\")){\n"
                        + "    " + algorithmName + "_mean<-c(" + algorithmName + "_mean,mean(scan(file" + algorithmName + ")))\n"
                        + "    " + algorithmName + "_median<-c(" + algorithmName + "_median,median(scan(file" + algorithmName + ")))\n"
                        + "  }\n"
                        + "  else{\n"
                        + "    " + algorithmName + "_mean<-c(mean(scan(file" + algorithmName + ")))\n"
                        + "    " + algorithmName + "_median<-c(median(scan(file" + algorithmName + ")))\n"
                        + "  }\n");
                os.write("\n");
            }
            os.write("}\n\n");
            os.write("algs<-c(");
            for (int i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write("\"" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "\",");
            } // for
            os.write("\"" + experiment.getAlgorithmList().get(experiment.getAlgorithmList().size() - 1).getAlgorithmTag() + "\")" + "\n");
            String[] colors = {"magenta", "black", "orange", "red", "green3", "blue"};
            
            /* MEAN */
            
            os.write("max<- max(");
            int i;
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_mean" + ",");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_mean" + ")\n");
            os.write("min<- min(0,");
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_mean" + ",");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_mean" + ")\n");
            os.write("plot(" + experiment.getAlgorithmList().get(0).getAlgorithmTag()+"_mean" + ",type=\"l\",col=\"" + colors[0] + "\", lty=1, ylim=c(min,max), , xaxt='n', xlab = \"Problem\", ylab = indicator)\n");
            os.write("axis(1, at=1:5, labels=problems)\n");
            int j = 2;
            for (i = 1; i < experiment.getAlgorithmList().size(); i++) {
                os.write("lines(" + experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_mean" + ",col=\"" + colors[i] + "\", lty=" + j + ")\n");
                j = (j == 1) ? 2 : 1;
            } // for
            os.write("legend(\"topleft\", legend=algs, col=c(\"" + colors[0] + "\", \"" + colors[1] + "\", \"" + colors[2] + "\", \"" + colors[3] + "\", \"" + colors[4] + "\", \"" + colors[5] + "\"), lty=1:2, cex=0.8)\n");

            os.write("titulo <-paste(indicator, \"Mean\", sep=\":\")" + "\n");
            os.write("title(main=titulo)" + "\n");
            
            os.write("\n");
            
            /* MEDIAN */
            
            os.write("max<- max(");
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_median" + ",");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_median" + ")\n");
            os.write("min<- min(0,");
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_median" + ",");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_median" + ")\n");
            os.write("plot(" + experiment.getAlgorithmList().get(0).getAlgorithmTag()+"_median" + ",type=\"l\",col=\"" + colors[0] + "\", lty=1, ylim=c(min,max), , xaxt='n', xlab = \"Problem\", ylab = indicator)\n");
            os.write("axis(1, at=1:5, labels=problems)\n");
            j = 2;
            for (i = 1; i < experiment.getAlgorithmList().size(); i++) {
                os.write("lines(" + experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_median" + ",col=\"" + colors[i] + "\", lty=" + j + ")\n");
                j = (j == 1) ? 2 : 1;
            } // for
            os.write("legend(\"topleft\", legend=algs, col=c(\"" + colors[0] + "\", \"" + colors[1] + "\", \"" + colors[2] + "\", \"" + colors[3] + "\", \"" + colors[4] + "\", \"" + colors[5] + "\"), lty=1:2, cex=0.8)\n");

            os.write("titulo <-paste(indicator, \"Median\", sep=\":\")" + "\n");
            os.write("title(main=titulo)" + "\n");
            
            os.write("\ndev.off()\n");

            os.write("}" + "\n");

            os.write("par(mfrow=c(" + numberOfRows + "," + numberOfColumns + "))" + "\n");

            os.write("indicator<-\"" + indicator.getName() + "\"" + "\n");

            os.write("qIndicator(indicator, c(\""+experiment.getProblemList().get(0).getTag()+"\"");
            for (i = 1; i < experiment.getProblemList().size(); i++) {
                os.write(", \"" + experiment.getProblemList().get(i).getTag() + "\"");
            }
            os.write("))" + "\n");

            os.close();

            //execute R script on Windows --> You need to set ENVIRONMENT VARIABLE on the R path (es. C:\Program Files\R\R-3.5.0\bin) 
            runRScript(rFileName);

        }

        /*
        Create TIMES.Plot.R
         */
        createTimesPlot("TIMES");
        createTimesPlot("TIMES10s");
        createTimesPlot("TIMES30s");
    }

    public void runRScript(String fileRPath) {
        try {
            Runtime.getRuntime().exec("Rscript " + fileRPath);
        } catch (IOException ex) {
            Logger.getLogger(GeneratePDFPlotsWithR.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createTimesPlot(String name) throws IOException {
        String rDirectoryName = experiment.getExperimentBaseDirectory() + "/" + DEFAULT_R_DIRECTORY;
        String finalPath = experiment.getExperimentBaseDirectory().replaceAll("\\\\", "/");

        String rFileName = rDirectoryName + "/" + name + ".Plot" + ".R";
        FileWriter os = new FileWriter(rFileName, false);
        os.write("pdf(\""
                + finalPath
                + "/R/"
                + name
                + ".Plot.pdf\")"
                + "\n");

        os.write("resultDirectory<-\""
                + finalPath
                + "/data" + "\"" + "\n");
        os.write("qIndicator <- function(indicator, problems)" + "\n");
        os.write("{" + "\n");

        os.write("for(problem in problems){\n");
            for (int i = 0; i < experiment.getAlgorithmList().size(); i++) {
                String algorithmName = experiment.getAlgorithmList().get(i).getAlgorithmTag();
                os.write("file" + algorithmName + "<-paste(resultDirectory, \"" + algorithmName + "\", sep=\"/\")" + "\n");
                os.write("file" + algorithmName + "<-paste(file" + algorithmName + ", " + "problem, sep=\"/\")" + "\n");
                os.write("file" + algorithmName + "<-paste(file" + algorithmName + ", " + "indicator, sep=\"/\")" + "\n");
                os.write("if (exists(\"" + algorithmName + "_mean\") && exists(\"" + algorithmName + "_median\")){\n"
                        + "    " + algorithmName + "_mean<-c(" + algorithmName + "_mean,mean(scan(file" + algorithmName + ")))\n"
                        + "    " + algorithmName + "_median<-c(" + algorithmName + "_median,median(scan(file" + algorithmName + ")))\n"
                        + "  }\n"
                        + "  else{\n"
                        + "    " + algorithmName + "_mean<-c(mean(scan(file" + algorithmName + ")))\n"
                        + "    " + algorithmName + "_median<-c(median(scan(file" + algorithmName + ")))\n"
                        + "  }\n");
                os.write("\n");
            }
            os.write("}\n\n");
            os.write("algs<-c(");
            for (int i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write("\"" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "\",");
            } // for
            os.write("\"" + experiment.getAlgorithmList().get(experiment.getAlgorithmList().size() - 1).getAlgorithmTag() + "\")" + "\n");
            String[] colors = {"magenta", "black", "orange", "red", "green3", "blue"};
            
            /* MEAN */
            
            os.write("max<- max(");
            int i;
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_mean" + ",");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_mean" + ")\n");
            os.write("min<- min(0,");
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_mean" + ",");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_mean" + ")\n");
            os.write("plot(" + experiment.getAlgorithmList().get(0).getAlgorithmTag()+"_mean" + ",type=\"l\",col=\"" + colors[0] + "\", lty=1, ylim=c(min,max), , xaxt='n', xlab = \"Problem\", ylab = indicator)\n");
            os.write("axis(1, at=1:5, labels=problems)\n");
            int j = 2;
            for (i = 1; i < experiment.getAlgorithmList().size(); i++) {
                os.write("lines(" + experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_mean" + ",col=\"" + colors[i] + "\", lty=" + j + ")\n");
                j = (j == 1) ? 2 : 1;
            } // for
            os.write("legend(\"topleft\", legend=algs, col=c(\"" + colors[0] + "\", \"" + colors[1] + "\", \"" + colors[2] + "\", \"" + colors[3] + "\", \"" + colors[4] + "\", \"" + colors[5] + "\"), lty=1:2, cex=0.8)\n");

            os.write("titulo <-paste(indicator, \"Mean\", sep=\":\")" + "\n");
            os.write("title(main=titulo)" + "\n");
            
            os.write("\n");
            
            /* MEDIAN */
            
            os.write("max<- max(");
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_median" + ",");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_median" + ")\n");
            os.write("min<- min(0,");
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_median" + ",");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_median" + ")\n");
            os.write("plot(" + experiment.getAlgorithmList().get(0).getAlgorithmTag()+"_median" + ",type=\"l\",col=\"" + colors[0] + "\", lty=1, ylim=c(min,max), , xaxt='n', xlab = \"Problem\", ylab = indicator)\n");
            os.write("axis(1, at=1:5, labels=problems)\n");
            j = 2;
            for (i = 1; i < experiment.getAlgorithmList().size(); i++) {
                os.write("lines(" + experiment.getAlgorithmList().get(i).getAlgorithmTag()+"_median" + ",col=\"" + colors[i] + "\", lty=" + j + ")\n");
                j = (j == 1) ? 2 : 1;
            } // for
            os.write("legend(\"topleft\", legend=algs, col=c(\"" + colors[0] + "\", \"" + colors[1] + "\", \"" + colors[2] + "\", \"" + colors[3] + "\", \"" + colors[4] + "\", \"" + colors[5] + "\"), lty=1:2, cex=0.8)\n");

            os.write("titulo <-paste(indicator, \"Median\", sep=\":\")" + "\n");
            os.write("title(main=titulo)" + "\n");
            
            os.write("\ndev.off()\n");

            os.write("}" + "\n");

            os.write("par(mfrow=c(" + numberOfRows + "," + numberOfColumns + "))" + "\n");

            os.write("indicator<-\"" + name + "\"" + "\n");

            os.write("qIndicator(indicator, c(\""+experiment.getProblemList().get(0).getTag()+"\"");
            for (i = 1; i < experiment.getProblemList().size(); i++) {
                os.write(", \"" + experiment.getProblemList().get(i).getTag() + "\"");
            }
            os.write("))" + "\n");

            os.close();

        //execute R script on Windows --> You need to set ENVIRONMENT VARIABLE on the R path (es. C:\Program Files\R\R-3.5.0\bin) 
        runRScript(rFileName);
    }
}
