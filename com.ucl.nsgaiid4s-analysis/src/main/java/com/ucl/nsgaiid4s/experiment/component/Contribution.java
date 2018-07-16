package com.ucl.nsgaiid4s.experiment.component;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.uma.jmetal.qualityindicator.impl.*;
import org.uma.jmetal.util.point.Point;

/**
 * This class implements the unary epsilon additive indicator as proposed in E.
 * Zitzler, E. Thiele, L. Laummanns, M., Fonseca, C., and Grunert da Fonseca. V
 * (2003): Performance Assessment of Multiobjective Optimizers: An Analysis and
 * Review. The code is the a Java version of the original metric implementation
 * by Eckart Zitzler. It can be used also as a command line program just by
 * typing $java org.uma.jmetal.qualityindicator.impl.Epsilon <solutionFrontFile>
 * <trueFrontFile> <getNumberOfObjectives>
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class Contribution<S extends Solution<?>> extends GenericIndicator<S> {

    /**
     * Default constructor
     */
    public Contribution() {
    }

    /**
     * Constructor
     *
     * @param referenceParetoFrontFile
     * @throws FileNotFoundException
     */
    public Contribution(String referenceParetoFrontFile) throws FileNotFoundException {
        super(referenceParetoFrontFile);
    }

    /**
     * Constructor
     *
     * @param referenceParetoFront
     */
    public Contribution(Front referenceParetoFront) {
        super(referenceParetoFront);
    }

    @Override
    public boolean isTheLowerTheIndicatorValueTheBetter() {
        return false;
    }

    /**
     * Evaluate() method
     *
     * @param solutionList
     * @return
     */
    @Override
    public Double evaluate(List<S> solutionList) {
        if (solutionList == null) {
            throw new JMetalException("The pareto front approximation list is null");
        }

        return contribution(new ArrayFront(solutionList), referenceParetoFront);
    }

    /**
     * Returns the value of the epsilon indicator.
     *
     * @param front Solution front
     * @param referenceFront Optimal Pareto front
     * @return the value of the epsilon indicator
     * @throws JMetalException
     */
    private double contribution(Front front, Front referenceFront) throws JMetalException {
        int contribution = 0;
        for (int i = 0; i < front.getNumberOfPoints(); i++) {
            if (contains(referenceFront, front.getPoint(i))) {
                contribution++;
            }
        }
        System.out.println("CONTRIBUTION: "+contribution);
        System.out.println("NUMBER: "+front.getNumberOfPoints());
        return contribution/front.getNumberOfPoints();
    }

    private boolean contains(Front front, Point point) {
        for (int i = 0; i < front.getNumberOfPoints(); i++) {
            if (Arrays.equals(point.getValues(), front.getPoint(i).getValues()))
                return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "Ic";
    }

    @Override
    public String getDescription() {
        return "Additive Epsilon quality indicator";
    }
}
