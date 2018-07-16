package com.ucl.nsgaiid4s.operator;




import java.io.Serializable;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.util.RepairDoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolutionAtBounds;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

/**
 * @author Fabiano Pecorelli
 * @author Carlo Di Domenico
 */
@SuppressWarnings("serial")
public class NSGAIID4SPolynomialMutation<S extends Solution<?>> extends PolynomialMutation {
 

  /** Constructor */
  public NSGAIID4SPolynomialMutation(double mutationProbability, double distributionIndex) {
    this(mutationProbability, distributionIndex, new RepairDoubleSolutionAtBounds()) ;
  }

  /** Constructor */
  public NSGAIID4SPolynomialMutation(double mutationProbability, double distributionIndex,
      RepairDoubleSolution solutionRepair) {
	  super(mutationProbability, distributionIndex, solutionRepair, (RandomGenerator<Double> & Serializable)() -> JMetalRandom.getInstance().nextDouble());
  }

}
