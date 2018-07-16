/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ucl.nsgaiid4s.rejectpolicy;

import com.ucl.nsgaiid4s.algorithm.DistributedAbstractGeneticAlgorithm;
import java.util.List;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

/**
 *
 * @author fably
 */
public class DominanceRankingRejectPolicy<S extends Solution> implements RejectPolicy<S> {

    private Ranking<S> ranking = new DominanceRanking<>();
    
    @Override
    public void applyStrategy(DistributedAbstractGeneticAlgorithm<S> algorithm, List<S> rejectedIndividuals, List<S> superPareto) {
        
        List<S> population = algorithm.getPopulation();
        
        for(S s : population)
            if (rejectedIndividuals.contains(s))
                ranking.setAttribute(s, Integer.MAX_VALUE);
    }

}
