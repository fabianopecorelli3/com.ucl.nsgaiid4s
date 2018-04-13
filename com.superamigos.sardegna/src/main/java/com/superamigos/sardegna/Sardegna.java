package com.superamigos.sardegna;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.List;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author Fabiano Pecorelli Carlo Di Domenico
 */
public class Sardegna<S extends Solution> extends AbstractGeneticAlgorithm<S, List<S>> {

    static JavaSparkContext sparkContext;
    private List<Population<S>> populations;
    private List<S> pareto;
    private ModifiedAbstractGeneticAlgorithm algorithm;
    private int numberOfPartitions;
    private int populationSize;
    private int evaluations;
    protected final int maxEvaluations;

    /**
     *
     * @param problem, the problem to solve
     * @param algorithm, the algorithm used to solve the problem
     * @param numberOfPartition, the number of partition to distribute in the
     * cluster
     * @param populationSize, the number of individuals for each partition
     * @param sparkContext
     *
     */
    public Sardegna(Problem<S> problem, int maxEvaluation, ModifiedAbstractGeneticAlgorithm algorithm, int numberOfPartitions, int populationSize, JavaSparkContext sparkContext) {
        super(problem);
        this.algorithm = algorithm;
        this.numberOfPartitions = numberOfPartitions;
        this.populationSize = populationSize;
        this.sparkContext = sparkContext;
        this.populations = new ArrayList<Population<S>>();
        for (int i = 0; i < this.numberOfPartitions; i++){
            populations.add(new Population<S>());
        }
        this.maxEvaluations = maxEvaluation;
        pareto = new ArrayList<>();
    }

    @Override
    public void run() {
        List<S> offspringPopulation;
        List<S> matingPopulation;
        /*Population<S> pop = populations.get(0);
        algorithm.executeIteration(pop);
        System.out.println("POPULATION "+pop.getElite());
        */
        JavaRDD<Population<S>> populationToParallelize = sparkContext.parallelize(populations, numberOfPartitions);
        initProgress();
        while (!isStoppingConditionReached()) {
            
            JavaRDD<Population<S>> evaluatedPopulation
                    = populationToParallelize.map(solutions -> {
                        return algorithm.executeIteration(solutions);
                    });
            populations = evaluatedPopulation.collect();
            for (Population<S> pop : populations)
                pareto.addAll(pop.getElite());
            updateProgress();
            //population = evaluatedPopulation.collect();
        }
    }

    @Override
    public List<S> getResult() {
        System.out.println("PARETOOOOOOOOOOO "+pareto);
        return pareto; //TODO: Calculate non-dominated solutions in the population.
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
        return numberOfPartitions*populationSize;
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

}
