package com.superamigos.sardegna.sardegna.algorithm.impl;

import com.superamigos.sardegna.sardegna.algorithm.ModifiedAbstractGeneticAlgorithm;
import com.superamigos.sardegna.sardegna.utils.PrinterUtils;
import com.superamigos.sardegna.sardegna.rejectpolicy.RejectPolicy;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class SardegnaNSGAII<S extends Solution<?>> extends ModifiedAbstractGeneticAlgorithm<S> {
  protected final int maxEvaluations;

  protected final SolutionListEvaluator<S> evaluator;

  protected int evaluations;

  /**
   * Constructor
   */
  public SardegnaNSGAII(Problem<S> problem, int maxEvaluations, int populationSize,
      CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
      SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
    super(problem);
    this.maxEvaluations = maxEvaluations;
    setMaxPopulationSize(populationSize); ;

    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;

    this.evaluator = evaluator;
  }

  @Override protected void initProgress() {
    evaluations = getMaxPopulationSize();
  }

  @Override protected void updateProgress() {
    evaluations += getMaxPopulationSize() ;
  }

  @Override protected boolean isStoppingConditionReached() {
    return evaluations >= maxEvaluations;
  }

  @Override protected List<S> evaluatePopulation(List<S> population) {
    population = evaluator.evaluate(population, getProblem());

    return population;
  }

  @Override protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    List<S> jointPopulation = new ArrayList<>();
    jointPopulation.addAll(population);
    jointPopulation.addAll(offspringPopulation);

    RankingAndCrowdingSelection<S> rankingAndCrowdingSelection ;
    rankingAndCrowdingSelection = new RankingAndCrowdingSelection<S>(getMaxPopulationSize()) ;

    return rankingAndCrowdingSelection.execute(jointPopulation) ;
  }

  @Override
  public void computePareto(){
      this.pareto = getNonDominatedSolutions(getPopulation());
  }

  protected List<S> getNonDominatedSolutions(List<S> solutionList) {
    return SolutionListUtils.getNondominatedSolutions(solutionList);
  }

  @Override public String getName() {
    return "NSGAII" ;
  }

  @Override public String getDescription() {
    return "Nondominated Sorting Genetic Algorithm version II" ;
  }
  
    @Override
    public void receiveRejectedIndividuals(List<S> rejectedIndividuals, RejectPolicy strategy, List<S> superPareto) {
        PrinterUtils.Printer.debug("CALLED");
        List<S> myRejected = new ArrayList<>();
        
        for (S s : rejectedIndividuals) {
            PrinterUtils.Printer.debug("REJECTED SOTTO: " + s);
        }
        
        for (S s : population) {
            PrinterUtils.Printer.debug("POPULATION SOTTO (2): " + s);
        }
        
        for (S s : rejectedIndividuals){
            if (population.contains(s))
                myRejected.add(s);
        }
        PrinterUtils.Printer.debug("Io ne devo scartare "+myRejected.size()+" dei "+rejectedIndividuals.size());
        
        if (myRejected.size() > 0)
            strategy.applyStrategy(this, myRejected, superPareto);
    }

    @Override
    public List<S> extraEvaluation(List<S> solutionList) {
        return evaluatePopulation(solutionList);
    }
}
