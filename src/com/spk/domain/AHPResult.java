package com.spk.domain;

import java.util.Map;

/**
 * Entity representing the result of AHP calculation.
 * Contains criteria weights and consistency ratio.
 */
public class AHPResult {
    private Map<Integer, Double> weights; // kriteriaId -> weight
    private double consistencyRatio;
    private double consistencyIndex;
    private double lambdaMax;
    private boolean consistent;

    // Intermediate matrices for display
    private double[][] pairwiseMatrix;
    private double[][] normalizedMatrix;
    private double[] priorityVector;

    public AHPResult() {}

    public Map<Integer, Double> getWeights() { return weights; }
    public void setWeights(Map<Integer, Double> weights) { this.weights = weights; }

    public double getConsistencyRatio() { return consistencyRatio; }
    public void setConsistencyRatio(double consistencyRatio) {
        this.consistencyRatio = consistencyRatio;
        this.consistent = consistencyRatio <= 0.1;
    }

    public double getConsistencyIndex() { return consistencyIndex; }
    public void setConsistencyIndex(double consistencyIndex) { this.consistencyIndex = consistencyIndex; }

    public double getLambdaMax() { return lambdaMax; }
    public void setLambdaMax(double lambdaMax) { this.lambdaMax = lambdaMax; }

    public boolean isConsistent() { return consistent; }

    public double[][] getPairwiseMatrix() { return pairwiseMatrix; }
    public void setPairwiseMatrix(double[][] pairwiseMatrix) { this.pairwiseMatrix = pairwiseMatrix; }

    public double[][] getNormalizedMatrix() { return normalizedMatrix; }
    public void setNormalizedMatrix(double[][] normalizedMatrix) { this.normalizedMatrix = normalizedMatrix; }

    public double[] getPriorityVector() { return priorityVector; }
    public void setPriorityVector(double[] priorityVector) { this.priorityVector = priorityVector; }
}
