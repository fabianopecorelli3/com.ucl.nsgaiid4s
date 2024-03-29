/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucl.nsgaiid4s.rejectpolicy;

import com.ucl.nsgaiid4s.algorithm.DistributedAbstractGeneticAlgorithm;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author fably
 */
public class RandomReplacementRejectPolicy<S extends Solution> implements RejectPolicy<S> {

    @Override
    public void applyStrategy(DistributedAbstractGeneticAlgorithm<S> algorithm, List<S> rejectedIndividuals, List<S> superPareto) {
        int howMuch = rejectedIndividuals.size();
        List<S> population = algorithm.getPopulation();
        List<S> newIndividuals = new ArrayList<>();
        population.removeAll(rejectedIndividuals);
        for (int i = 0; i < howMuch; i++) {
            S newIndividual = algorithm.getProblem().createSolution();
            newIndividuals.add(newIndividual);
        }
        newIndividuals = algorithm.extraEvaluation(newIndividuals);
      
        population.addAll(newIndividuals);
        algorithm.setPopulation(population);
    }

}
