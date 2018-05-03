/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.sardegna.core;

import com.superamigos.sardegna.sardegna.algorithm.impl.Sardegna;
import com.superamigos.sardegna.sardegna.operator.impl.crossover.SardegnaSBXCrossover;
import com.superamigos.sardegna.sardegna.operator.impl.mutation.SardegnaPolynomialMutation;
import com.superamigos.sardegna.sardegna.rejectpolicy.DominanceRankingRejectPolicy;
import com.superamigos.sardegna.sardegna.rejectpolicy.RandomReplacementRejectPolicy;
import com.superamigos.sardegna.sardegna.utils.PrinterUtils;
import com.superamigos.sardegna.sardegna.rejectpolicy.RemoveRejectPolicy;
import com.superamigos.sardegna.sardegna.rejectpolicy.ReplacementRejectPolicy;
import com.superamigos.sardegna.sardegna.rejectpolicy.ReproductionRejectPolicy;
import com.superamigos.sardegna.sardegna.utils.GenerateExcelResultsFile;
import com.superamigos.sardegna.sardegna.utils.GenerateLatexTablesWithStatistics;
import com.superamigos.sardegna.sardegna.utils.GeneratePDFBoxplotsWithR;
import com.superamigos.sardegna.sardegna.utils.SardegnaExperimentAlgorithm;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.spark.api.java.JavaSparkContext;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT1;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT2;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT3;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT4;
import org.uma.jmetal.problem.multiobjective.zdt.ZDT6;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentBuilder;
import org.uma.jmetal.util.experiment.component.ComputeQualityIndicators;
import org.uma.jmetal.util.experiment.component.ExecuteAlgorithms;
import org.uma.jmetal.util.experiment.component.GenerateBoxplotsWithR;
import org.uma.jmetal.util.experiment.component.GenerateFriedmanTestTables;
import org.uma.jmetal.util.experiment.component.GenerateWilcoxonTestTablesWithR;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

/**
 *
 * @author fably
 */
public class SmallRunner {

    private static final int INDEPENDENT_RUNS = 2;
    private String path;
    private JavaSparkContext sparkContext;

    public SmallRunner() {
    }

    public SmallRunner(String path, JavaSparkContext sparkContext) {
        this.path = path;
        this.sparkContext = sparkContext;
    }

    public void run() throws FileNotFoundException, IOException {
        String experimentBaseDirectory = path;

        PrinterUtils.Printer.setPw("my-log.txt");

        List<ExperimentProblem<DoubleSolution>> problemList = new ArrayList<>();
        problemList.add(new ExperimentProblem<>(new ZDT1()));
        problemList.add(new ExperimentProblem<>(new ZDT2()));
        problemList.add(new ExperimentProblem<>(new ZDT3()));
        problemList.add(new ExperimentProblem<>(new ZDT4()));
        problemList.add(new ExperimentProblem<>(new ZDT6()));

        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList
                = configureAlgorithmList(problemList);
        List<String> referenceFrontFileNames = Arrays.asList("ZDT1.pf", "ZDT2.pf", "ZDT3.pf", "ZDT4.pf", "ZDT6.pf");

        Experiment<DoubleSolution, List<DoubleSolution>> experiment
                = new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>("NSGAIIStudy")
                .setAlgorithmList(algorithmList)
                .setProblemList(problemList)
                .setExperimentBaseDirectory(experimentBaseDirectory)
                .setOutputParetoFrontFileName("FUN")
                .setOutputParetoSetFileName("VAR")
                .setReferenceFrontDirectory("/pareto_fronts")
                .setReferenceFrontFileNames(referenceFrontFileNames)
                .setIndicatorList(Arrays.asList(
                        new Epsilon<DoubleSolution>(),
                        new Spread<DoubleSolution>(),
                        new GenerationalDistance<DoubleSolution>(),
                        new PISAHypervolume<DoubleSolution>(),
                        new InvertedGenerationalDistance<DoubleSolution>(),
                        new InvertedGenerationalDistancePlus<DoubleSolution>()))
                .setIndependentRuns(INDEPENDENT_RUNS)
                .setNumberOfCores(8)
                .build();

        new ExecuteAlgorithms<>(experiment).run();
        new ComputeQualityIndicators<>(experiment).run();
        new GenerateExcelResultsFile(experiment).run();
        new GenerateLatexTablesWithStatistics(experiment).run();
        new GenerateWilcoxonTestTablesWithR<>(experiment).run();
        new GenerateFriedmanTestTables<>(experiment).run();
        new GeneratePDFBoxplotsWithR<>(experiment).setRows(1).setColumns(1).run();
        PrinterUtils.Printer.closePw();
    }

    /**
     * The algorithm list is composed of pairs
     * {@link Algorithm} + {@link Problem} which form part of a
     * {@link SardegnaExperimentAlgorithm}, which is a decorator for class
     * {@link Algorithm}. The {@link
     * SardegnaExperimentAlgorithm} has an optional tag component, that can be set as it
     * is shown in this example, where four variants of a same algorithm are
     * defined.
     */
    public List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> configureAlgorithmList(
            List<ExperimentProblem<DoubleSolution>> problemList) {
        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = new ArrayList<>();

        for (int i = 0; i < problemList.size(); i++) {
            Algorithm<List<DoubleSolution>> algorithm = new NSGAIIBuilder<>(
                    problemList.get(i).getProblem(),
                    new SBXCrossover(1.0, 5),
                    new PolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0))
                    .setMaxEvaluations(2500)
                    .setPopulationSize(250)
                    .build();
            algorithms.add(new SardegnaExperimentAlgorithm<>(algorithm, "jMetal", problemList.get(i).getTag()));
            Algorithm<List<DoubleSolution>> sardegna_RR = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    2500,
                    5,
                    250,
                    sparkContext,
                    new RandomReplacementRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0)
            );
            algorithms.add(new SardegnaExperimentAlgorithm<>(sardegna_RR, "RR", problemList.get(i).getTag()));
            Algorithm<List<DoubleSolution>> sardegna_REPL = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    2500,
                    5,
                    250,
                    sparkContext,
                    new ReplacementRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0)
            );
            algorithms.add(new SardegnaExperimentAlgorithm<>(sardegna_REPL, "RPL", problemList.get(i).getTag()));
            Algorithm<List<DoubleSolution>> sardegna_DR = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    2500,
                    5,
                    250,
                    sparkContext,
                    new DominanceRankingRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0)
            );
            algorithms.add(new SardegnaExperimentAlgorithm<>(sardegna_DR, "DR", problemList.get(i).getTag()));
            Algorithm<List<DoubleSolution>> sardegna_REPR = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    2500,
                    5,
                    250,
                    sparkContext,
                    new ReproductionRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0)
            );
            algorithms.add(new SardegnaExperimentAlgorithm<>(sardegna_REPR, "RPR", problemList.get(i).getTag()));
            Algorithm<List<DoubleSolution>> sardegna_RM = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    2500,
                    5,
                    250,
                    sparkContext,
                    new RemoveRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0)
            );
            algorithms.add(new SardegnaExperimentAlgorithm<>(sardegna_RM, "RMV", problemList.get(i).getTag()));
        }
        return algorithms;
    }

}
