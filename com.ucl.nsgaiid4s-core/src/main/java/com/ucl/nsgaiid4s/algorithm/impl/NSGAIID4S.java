package com.ucl.nsgaiid4s.algorithm.impl;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.ucl.nsgaiid4s.algorithm.DistributedAbstractGeneticAlgorithm;
import com.ucl.nsgaiid4s.utils.PrinterUtils;
import com.ucl.nsgaiid4s.rejectpolicy.RejectPolicy;
import java.util.ArrayList;
import java.util.List;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

/**
 * @author Fabiano Pecorelli
 * @author Carlo Di Domenico
 */
public class NSGAIID4S<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {

    static JavaSparkContext sparkContext;
    private List<DistributedAbstractGeneticAlgorithm> algorithms;
    private List<S> pareto;
    private int numberOfPartitions;
    private int populationSize;
    private int evaluations;
    protected final int maxEvaluations;
    private List<S> rejectedIndividuals;
    private RejectPolicy<S> rejectPolicy;
    private Ranking<S> ranking;
    private int k;
    private int numberOfThreads;

    /**
     *
     * @param problem, the problem to solve
     * @param numberOfPartitions, the number of partition to distribute in the
     * cluster
     * @param populationSize, the total number of individuals
     * @param sparkContext
     *
     */
    public NSGAIID4S(Problem<S> problem, int maxEvaluations, int k, int numberOfPartitions, int populationSize, JavaSparkContext sparkContext, RejectPolicy rejectPolicy, CrossoverOperator crossoverOperator, MutationOperator mutationOperator, int numberOfThreads) {
        super(problem);
        this.numberOfPartitions = numberOfPartitions;
        this.algorithms = new ArrayList<>();
        this.k = k;
        for (int i = 0; i < numberOfPartitions; i++) {
            algorithms.add(new NSGAIIBuilder<S>(
                    problem,
                    crossoverOperator,
                    mutationOperator,
                    numberOfThreads)
                    .setMaxEvaluations(maxEvaluations / numberOfPartitions)
                    .setPopulationSize(populationSize / numberOfPartitions)
                    .build());
        }

        this.pareto = new ArrayList<S>();
        this.populationSize = populationSize;
        this.sparkContext = sparkContext;
        this.maxEvaluations = maxEvaluations;
        this.rejectedIndividuals = new ArrayList<>();
        this.rejectPolicy = rejectPolicy;
        this.ranking = new DominanceRanking<>();
        this.numberOfThreads = numberOfThreads;
    }

    @Override
    public void run() {

        JavaRDD<DistributedAbstractGeneticAlgorithm> algorithmsToParallelize = sparkContext.parallelize(algorithms, numberOfPartitions);
        initProgress();

        int i = 0;
        while (!isStoppingConditionReached()) {
            long timeStartIteration = System.currentTimeMillis();
            PrinterUtils.Printer.info("Start iteration #" + i, true);

            algorithmsToParallelize = algorithmsToParallelize.map(algorithm -> {
                algorithm.executeIteration();
                return algorithm;
            });

            if (((i + 1) % k) == 0) {

                long timeStartCollect = System.currentTimeMillis();
                PrinterUtils.Printer.info("START Collect phase", true);
                PrinterUtils.Printer.info("ENDED Collect phase in: " + (System.currentTimeMillis() - timeStartCollect) + " ms", true);

                JavaRDD<List<S>> pareti = algorithmsToParallelize.map(algorithm -> {
                    if ((rejectedIndividuals.size() > 0)) {
                        algorithm.receiveRejectedIndividuals(rejectedIndividuals, rejectPolicy, pareto);
                    }
                    return algorithm.getResult();
                });

                pareto = pareti.reduce((a, b) -> {
                    return computeSuperPareto(a, b);
                });
                
                algorithms = algorithmsToParallelize.collect();
                algorithmsToParallelize = sparkContext.parallelize(algorithms, numberOfPartitions);
            }
            
            updateProgress();
            PrinterUtils.Printer.info("ENDED Iteration #" + i + " in: " + (System.currentTimeMillis() - timeStartIteration) + " ms", true);
            i++;

        }
    }

    @Override
    public List<S> getResult() {
        return pareto;
    }

    @Override
    public String getName() {
        return "Sardegna";
    }

    @Override
    public String getDescription() {
        return "Algorithm description"; //TODO
    }

    @Override
    protected void initProgress() {
        evaluations = getMaxPopulationSize();
    }

    @Override
    protected void updateProgress() {
        evaluations += getMaxPopulationSize();
    }

    @Override
    public int getMaxPopulationSize() {
        return populationSize;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return evaluations >= maxEvaluations;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected List<S> replacement(List<S> list, List<S> list1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<S> computeSuperPareto(List<S> a, List<S> b) {
        List<S> union = new ArrayList<>();
        List<S> nonDominated = new ArrayList<>();
        union.addAll(a);
        union.addAll(b);
        nonDominated = getNonDominatedSolutions(union);
        rejectedIndividuals.clear();
        for (S s : union) {
            if (!nonDominated.contains(s)) {
                ranking.setAttribute(s, 0);
                rejectedIndividuals.add(s);
            }
        }
        return nonDominated;
    }

    protected List<S> getNonDominatedSolutions(List<S> solutionList) {
        return SolutionListUtils.getNondominatedSolutions(solutionList);
    }

}
