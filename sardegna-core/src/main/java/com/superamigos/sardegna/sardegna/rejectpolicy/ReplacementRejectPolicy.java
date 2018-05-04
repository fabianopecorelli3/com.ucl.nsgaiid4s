/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.sardegna.rejectpolicy;

import com.superamigos.sardegna.sardegna.algorithm.ModifiedAbstractGeneticAlgorithm;
import com.superamigos.sardegna.sardegna.utils.PrinterUtils;
import java.util.List;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author fably
 */
public class ReplacementRejectPolicy<S extends Solution> implements RejectPolicy<S> {

    @Override
    public void applyStrategy(ModifiedAbstractGeneticAlgorithm<S> algorithm, List<S> rejectedIndividuals, List<S> superPareto) {
        int howMuch = rejectedIndividuals.size();
        List<S> population = algorithm.getPopulation();
        population.removeAll(rejectedIndividuals);
        for (int i = 0; i < howMuch; i++) {
            S newIndividual = algorithm.getSelectionOperator().execute(superPareto); //TOASK Posso prendere 2 volte lo stesso parent? no
            population.add(newIndividual);
        }
        algorithm.setPopulation(population);
    }

}
