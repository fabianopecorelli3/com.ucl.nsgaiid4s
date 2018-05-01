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
import org.uma.jmetal.problem.multiobjective.wfg.WFG1;
import org.uma.jmetal.problem.multiobjective.wfg.WFG2;
import org.uma.jmetal.problem.multiobjective.wfg.WFG3;
import org.uma.jmetal.problem.multiobjective.wfg.WFG4;
import org.uma.jmetal.problem.multiobjective.wfg.WFG5;
import org.uma.jmetal.problem.multiobjective.wfg.WFG6;
import org.uma.jmetal.problem.multiobjective.wfg.WFG7;
import org.uma.jmetal.problem.multiobjective.wfg.WFG8;
import org.uma.jmetal.problem.multiobjective.wfg.WFG9;
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
public class SardegnaWFGRunner {

    private static final int INDEPENDENT_RUNS = 10;
    private String path;
    private JavaSparkContext sparkContext;

    public SardegnaWFGRunner() {
    }

    public SardegnaWFGRunner(String path, JavaSparkContext sparkContext) {
        this.path = path;
        this.sparkContext = sparkContext;
    }

    public void run() throws FileNotFoundException, IOException {
        String experimentBaseDirectory = path;

        PrinterUtils.Printer.setPw("my-log.txt");

        List<ExperimentProblem<DoubleSolution>> problemList = new ArrayList<>();
        problemList.add(new ExperimentProblem<>(new WFG1()));
        problemList.add(new ExperimentProblem<>(new WFG2()));
        problemList.add(new ExperimentProblem<>(new WFG3()));
        problemList.add(new ExperimentProblem<>(new WFG4()));
        problemList.add(new ExperimentProblem<>(new WFG5()));
        problemList.add(new ExperimentProblem<>(new WFG6()));
        problemList.add(new ExperimentProblem<>(new WFG7()));
        problemList.add(new ExperimentProblem<>(new WFG8()));
        problemList.add(new ExperimentProblem<>(new WFG9()));

        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList
                = configureAlgorithmList(problemList);
        List<String> referenceFrontFileNames = Arrays.asList("WFG1.2D.pf", "WFG2.2D.pf", "WFG3.2D.pf", "WFG4.2D.pf", "WFG5.2D.pf", "WFG6.2D.pf", "WFG7.2D.pf", "WFG8.2D.pf", "WFG9.2D.pf");

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
        new GenerateBoxplotsWithR<>(experiment).setRows(3).setColumns(3).run();
        PrinterUtils.Printer.closePw();
    }

    /**
     * The algorithm list is composed of pairs
     * {@link Algorithm} + {@link Problem} which form part of a
     * {@link ExperimentAlgorithm}, which is a decorator for class
     * {@link Algorithm}. The {@link
     * ExperimentAlgorithm} has an optional tag component, that can be set as it
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
                    .setMaxEvaluations(25000)
                    .setPopulationSize(250)
                    .build();
            algorithms.add(new ExperimentAlgorithm<>(algorithm, "jMetal", problemList.get(i).getTag()));
            Algorithm<List<DoubleSolution>> sardegna_RR = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    25000,
                    5,
                    250,
                    sparkContext,
                    new RandomReplacementRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0)
            );
            algorithms.add(new ExperimentAlgorithm<>(sardegna_RR, "RandomReplacement", problemList.get(i).getTag()));
            Algorithm<List<DoubleSolution>> sardegna_REPL = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    25000,
                    5,
                    250,
                    sparkContext,
                    new ReplacementRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0)
            );
            algorithms.add(new ExperimentAlgorithm<>(sardegna_REPL, "Replacement", problemList.get(i).getTag()));
            Algorithm<List<DoubleSolution>> sardegna_DR = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    25000,
                    5,
                    250,
                    sparkContext,
                    new DominanceRankingRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0)
            );
            algorithms.add(new ExperimentAlgorithm<>(sardegna_DR, "DominanceRanking", problemList.get(i).getTag()));
            Algorithm<List<DoubleSolution>> sardegna_REPR = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    25000,
                    5,
                    250,
                    sparkContext,
                    new ReproductionRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0)
            );
            algorithms.add(new ExperimentAlgorithm<>(sardegna_REPR, "Reproduction", problemList.get(i).getTag()));
            Algorithm<List<DoubleSolution>> sardegna_RM = new Sardegna<DoubleSolution>(
                    problemList.get(i).getProblem(),
                    25000,
                    5,
                    250,
                    sparkContext,
                    new RemoveRejectPolicy(),
                    new SardegnaSBXCrossover(1.0, 5),
                    new SardegnaPolynomialMutation(1.0 / problemList.get(i).getProblem().getNumberOfVariables(), 10.0)
            );
            algorithms.add(new ExperimentAlgorithm<>(sardegna_RM, "Remove", problemList.get(i).getTag()));
        }
        return algorithms;
    }

}