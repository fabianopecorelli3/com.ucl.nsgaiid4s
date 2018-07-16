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
public class GeneratePDFWilcoxonWithR<Result> implements ExperimentComponent {

    private static final String DEFAULT_R_DIRECTORY = "R";

    private final Experiment<?, Result> experiment;
    private int numberOfRows;
    private int numberOfColumns;
    private boolean displayNotch;

    public GeneratePDFWilcoxonWithR(Experiment<?, Result> experimentConfiguration) {
        this.experiment = experimentConfiguration;

        displayNotch = false;
        numberOfRows = 1;
        numberOfColumns = 1;

        experiment.removeDuplicatedAlgorithms();
    }

    public GeneratePDFWilcoxonWithR<Result> setRows(int rows) {
        numberOfRows = rows;

        return this;
    }

    public GeneratePDFWilcoxonWithR<Result> setColumns(int columns) {
        numberOfColumns = columns;

        return this;
    }

    public GeneratePDFWilcoxonWithR<Result> setDisplayNotch() {
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
            String rFileName = rDirectoryName + "/" + indicator.getName() + ".Wilcoxon" + ".R";
            FileWriter os = new FileWriter(rFileName, false);
            os.write("library(gridExtra)\nlibrary(grid)\nlibrary(effsize)\nlibrary(reshape)\n\n");
            os.write("pdf(\""
                    + finalPath
                    + "/R/"
                    + indicator.getName()
                    + ".pval-effsize.pdf\")"
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

            for (int i = 0; i < experiment.getAlgorithmList().size(); i++) {
                os.write("#" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "\n");
                os.write("wt <- wilcox.test(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + ", " + experiment.getAlgorithmList().get(0).getAlgorithmTag() + ",paired = FALSE, exact=FALSE)\n" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_pvalues <- c(round(wt$p.value, digits = 5))\n");
                os.write("df <- data.frame(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "," + experiment.getAlgorithmList().get(0).getAlgorithmTag() + ")\n"
                        + "mdf <- melt(df, measure.vars=1:2)\n"
                        + "" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_effsize <- c(round(VD.A(mdf$value, mdf$variable, digits = 2)$estimate))\n");
                for (int j = 1; j < experiment.getAlgorithmList().size(); j++) {
                    os.write("wt <- wilcox.test(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + ", " + experiment.getAlgorithmList().get(j).getAlgorithmTag() + ",paired = FALSE, exact=FALSE)\n" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_pvalues <- c(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_pvalues, round(wt$p.value, digits = 5))\n\n");
                    os.write("df <- data.frame(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "," + experiment.getAlgorithmList().get(j).getAlgorithmTag() + ")\n"
                            + "mdf <- melt(df, measure.vars=1:2)\n"
                            + "" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_effsize <- c(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_effsize, round(VD.A(mdf$value, mdf$variable, digits = 2)$estimate))\n");

                }
            } // for

            os.write("mat <- matrix(c(");
            int i;
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_pvalues, ");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_pvalues");
            os.write("), nrow = 6, ncol = 6, dimnames= list(algs, algs))\n");
            os.write("table <- tableGrob(mat)\n"
                    + "h <- grobHeight(table)\n"
                    + "w <- grobWidth(table)\n"
                    + "title <- textGrob(\"P-Value\", y=unit(0.5,\"npc\") + h, \n"
                    + "                  vjust=0, gp=gpar(fontsize=20))\n"
                    + "gt <- gTree(children=gList(table, title))\n"
                    + "grid.draw(gt)\n\n");
            os.write("grid.newpage()\n");
            os.write("mat <- matrix(c(");
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_effsize, ");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_effsize");
            os.write("), nrow = 6, ncol = 6, dimnames= list(algs, algs))\n");
            os.write("table <- tableGrob(mat)\n"
                    + "h <- grobHeight(table)\n"
                    + "w <- grobWidth(table)\n"
                    + "title <- textGrob(\"Effect Size\", y=unit(0.5,\"npc\") + h, \n"
                    + "                  vjust=0, gp=gpar(fontsize=20))\n"
                    + "gt <- gTree(children=gList(table, title))\n"
                    + "grid.draw(gt)\n\n");
            os.write("dev.off()\n");
            os.write("}" + "\n");

            os.write("par(mfrow=c(" + numberOfRows + "," + numberOfColumns + "))" + "\n");

            os.write("indicator<-\"" + indicator.getName() + "\"" + "\n");

            for (ExperimentProblem<?> problem : experiment.getProblemList()) {
                os.write("qIndicator(indicator, \"" + problem.getTag() + "\")" + "\n");
            }

            os.close();

            //execute R script on Windows --> You need to set ENVIRONMENT VARIABLE on the R path (es. C:\Program Files\R\R-3.5.0\bin) 
            runRScript(rFileName);

        }

        /*
        Create TIMES.Boxplot.R
         */
        createTimesBoxplot("TIMES");
        createTimesBoxplot("TIMES10s");
        createTimesBoxplot("TIMES30s");
    }

    public void runRScript(String fileRPath) {
        try {
            Runtime.getRuntime().exec("Rscript " + fileRPath);
        } catch (IOException ex) {
            Logger.getLogger(GeneratePDFWilcoxonWithR.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createTimesBoxplot(String name) throws IOException {
        String rDirectoryName = experiment.getExperimentBaseDirectory() + "/" + DEFAULT_R_DIRECTORY;
        File rOutput;
        String finalPath = experiment.getExperimentBaseDirectory().replaceAll("\\\\", "/");

        String rFileName = rDirectoryName + "/"+name+".Wilcoxon" + ".R";
        FileWriter os = new FileWriter(rFileName, false);
        os.write("library(gridExtra)\nlibrary(grid)\nlibrary(effsize)\nlibrary(reshape)\n\n");
        os.write("pdf(\""
                + finalPath
                + "/R/"
                + name
                + ".pval-effsize.pdf\")"
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

        for (int i = 0; i < experiment.getAlgorithmList().size(); i++) {
            os.write("#" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "\n");
            os.write("wt <- wilcox.test(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + ", " + experiment.getAlgorithmList().get(0).getAlgorithmTag() + ",paired = FALSE, exact=FALSE)\n" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_pvalues <- c(round(wt$p.value, digits = 5))\n");
            os.write("df <- data.frame(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "," + experiment.getAlgorithmList().get(0).getAlgorithmTag() + ")\n"
                    + "mdf <- melt(df, measure.vars=1:2)\n"
                    + "" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_effsize <- c(round(VD.A(mdf$value, mdf$variable, digits = 2)$estimate))\n");
            for (int j = 1; j < experiment.getAlgorithmList().size(); j++) {
                os.write("wt <- wilcox.test(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + ", " + experiment.getAlgorithmList().get(j).getAlgorithmTag() + ",paired = FALSE, exact=FALSE)\n" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_pvalues <- c(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_pvalues, round(wt$p.value, digits = 5))\n\n");
                os.write("df <- data.frame(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "," + experiment.getAlgorithmList().get(j).getAlgorithmTag() + ")\n"
                        + "mdf <- melt(df, measure.vars=1:2)\n"
                        + "" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_effsize <- c(" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_effsize, round(VD.A(mdf$value, mdf$variable, digits = 2)$estimate))\n");

            }
        } // for
        os.write("mat <- matrix(c(");
            int i;
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_pvalues, ");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_pvalues");
            os.write("), nrow = 6, ncol = 6, dimnames= list(algs, algs))\n");
            os.write("table <- tableGrob(mat)\n"
                    + "h <- grobHeight(table)\n"
                    + "w <- grobWidth(table)\n"
                    + "title <- textGrob(\"P-Value\", y=unit(0.5,\"npc\") + h, \n"
                    + "                  vjust=0, gp=gpar(fontsize=20))\n"
                    + "gt <- gTree(children=gList(table, title))\n"
                    + "grid.draw(gt)\n\n");
            os.write("grid.newpage()\n");
            os.write("mat <- matrix(c(");
            for (i = 0; i < experiment.getAlgorithmList().size() - 1; i++) {
                os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_effsize, ");
            }
            os.write(experiment.getAlgorithmList().get(i).getAlgorithmTag() + "_effsize");
            os.write("), nrow = 6, ncol = 6, dimnames= list(algs, algs))\n");
            os.write("table <- tableGrob(mat)\n"
                    + "h <- grobHeight(table)\n"
                    + "w <- grobWidth(table)\n"
                    + "title <- textGrob(\"Effect Size\", y=unit(0.5,\"npc\") + h, \n"
                    + "                  vjust=0, gp=gpar(fontsize=20))\n"
                    + "gt <- gTree(children=gList(table, title))\n"
                    + "grid.draw(gt)\n\n");
            os.write("dev.off()\n");

        os.write("}" + "\n");

        os.write("par(mfrow=c(" + numberOfRows + "," + numberOfColumns + "))" + "\n");

        os.write("indicator<-\"" + name + "\"" + "\n");

        for (ExperimentProblem<?> problem : experiment.getProblemList()) {
            os.write("qIndicator(indicator, \"" + problem.getTag() + "\")" + "\n");
        }

        os.close();

        //execute R script on Windows --> You need to set ENVIRONMENT VARIABLE on the R path (es. C:\Program Files\R\R-3.5.0\bin) 
        runRScript(rFileName);
    }
}
