package com.superamigos.sardegna.sardegna.algorithm.impl;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.superamigos.sardegna.sardegna.algorithm.ModifiedAbstractGeneticAlgorithm;
import com.superamigos.sardegna.sardegna.utils.PrinterUtils;
import com.superamigos.sardegna.sardegna.rejectpolicy.RejectPolicy;
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
public class Sardegna<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {

    static JavaSparkContext sparkContext;
    private List<ModifiedAbstractGeneticAlgorithm> algorithms;
    private List<S> pareto;
    private int numberOfPartitions;
    private int populationSize;
    private int evaluations;
    protected final int maxEvaluations;
    private List<S> rejectedIndividuals;
    private RejectPolicy<S> rejectPolicy;
    private Ranking<S> ranking;

    /**
     *
     * @param problem, the problem to solve
     * @param numberOfPartitions, the number of partition to distribute in the
     * cluster
     * @param populationSize, the total number of individuals
     * @param sparkContext
     *
     */
    public Sardegna(Problem<S> problem, int maxEvaluations, int numberOfPartitions, int populationSize, JavaSparkContext sparkContext, RejectPolicy rejectPolicy, CrossoverOperator crossoverOperator, MutationOperator mutationOperator) {
        super(problem);
        this.numberOfPartitions = numberOfPartitions;
        this.algorithms = new ArrayList<>();
        
        for (int i = 0; i < numberOfPartitions; i++) {
            algorithms.add(new SardegnaNSGAIIBuilder<S>(
                    problem,
                    crossoverOperator,
                    mutationOperator)
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
    }

    @Override
    public void run() {

        JavaRDD<ModifiedAbstractGeneticAlgorithm> algorithmsToParallelize = sparkContext.parallelize(algorithms, numberOfPartitions);
        initProgress();

        int i = 0;
        while (!isStoppingConditionReached()) {

            PrinterUtils.Printer.print("EXECUTING ITERATION " + i + "\n\n");

            algorithmsToParallelize = sparkContext.parallelize(algorithms, numberOfPartitions);

            algorithmsToParallelize = algorithmsToParallelize.map(algorithm -> {
                if (rejectedIndividuals.size() > 0) {
                    PrinterUtils.Printer.print(new java.util.Date() + " - I rejcted inviati sono " + rejectedIndividuals.size() + "\n\n");
                    algorithm.receiveRejectedIndividuals(rejectedIndividuals, rejectPolicy, pareto);
                }
                algorithm.executeIteration();
                return algorithm;
            });

            algorithms = algorithmsToParallelize.collect();

            algorithmsToParallelize = sparkContext.parallelize(algorithms, numberOfPartitions);

            JavaRDD<List<S>> pareti = algorithmsToParallelize.map(algorithm -> {
                return algorithm.getResult();
            });

            pareto = pareti.reduce((a, b) -> {
                return computeSuperPareto(a, b);
            });

            updateProgress();
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
        for (S s : rejectedIndividuals) {
            PrinterUtils.Printer.print(new java.util.Date() + " - REJECTED: " + s + "\n\n");
        }
        return nonDominated;
    }

    protected List<S> getNonDominatedSolutions(List<S> solutionList) {
        return SolutionListUtils.getNondominatedSolutions(solutionList);
    }

}
