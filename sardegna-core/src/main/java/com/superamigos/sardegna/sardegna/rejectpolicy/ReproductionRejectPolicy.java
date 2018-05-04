/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.superamigos.sardegna.sardegna.rejectpolicy;

import com.superamigos.sardegna.sardegna.algorithm.ModifiedAbstractGeneticAlgorithm;
import com.superamigos.sardegna.sardegna.utils.PrinterUtils;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author fably
 */
public class ReproductionRejectPolicy<S extends Solution> implements RejectPolicy<S> {

    @Override
    public void applyStrategy(ModifiedAbstractGeneticAlgorithm<S> algorithm, List<S> rejectedIndividuals, List<S> superPareto) {
      
        int numberOfParents = algorithm.getCrossoverOperator().getNumberOfParents();
        algorithm.checkNumberOfParents(algorithm.getPopulation(), numberOfParents);
        List<S> population = algorithm.getPopulation();
        for (S s : rejectedIndividuals) {
            List<S> parents = new ArrayList<>(numberOfParents);
            List<S> offspringPop = new ArrayList<>(numberOfParents);
            parents.add(s);
            population.remove(s);
            for (int i = 1; i < numberOfParents; i++) {
                S partner = algorithm.getSelectionOperator().execute(superPareto); //TOASK Posso prendere 2 volte lo stesso parent?
                parents.add(partner);
                population.remove(partner);
            }
            offspringPop = algorithm.getCrossoverOperator().execute(parents);
            for (S sol : offspringPop) {
                algorithm.getMutationOperator().execute(sol);
            }
            population.addAll(offspringPop);
            algorithm.setPopulation(population);
        }

    }

}
