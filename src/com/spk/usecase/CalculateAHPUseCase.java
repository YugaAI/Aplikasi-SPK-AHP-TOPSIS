package com.spk.usecase;

import com.spk.domain.AHPResult;
import com.spk.domain.Criteria;
import com.spk.domain.PairwiseComparison;
import com.spk.repository.CriteriaRepository;
import com.spk.repository.PairwiseRepository;
import com.spk.repository.ResultRepository;

import java.sql.SQLException;
import java.util.*;

/**
 * Use case for AHP (Analytic Hierarchy Process) calculation.
 * 
 * AHP Steps:
 * 1. Build pairwise comparison matrix
 * 2. Normalize the matrix (divide each element by its column sum)
 * 3. Calculate priority vector (average of each row in normalized matrix)
 * 4. Calculate Consistency Index (CI) and Consistency Ratio (CR)
 * 5. CR must be <= 0.1 for the comparison to be considered consistent
 */
public class CalculateAHPUseCase {
    private final CriteriaRepository criteriaRepository;
    private final PairwiseRepository pairwiseRepository;
    private final ResultRepository resultRepository;

    // Random Consistency Index values for matrix sizes 1-15
    // Source: Saaty (1980)
    private static final double[] RI = {
            0.0,   // n=1
            0.0,   // n=2
            0.58,  // n=3
            0.90,  // n=4
            1.12,  // n=5
            1.24,  // n=6
            1.32,  // n=7
            1.41,  // n=8
            1.45,  // n=9
            1.49,  // n=10
            1.51,  // n=11
            1.48,  // n=12
            1.56,  // n=13
            1.57,  // n=14
            1.59   // n=15
    };

    public CalculateAHPUseCase() {
        this.criteriaRepository = new CriteriaRepository();
        this.pairwiseRepository = new PairwiseRepository();
        this.resultRepository = new ResultRepository();
    }

    /**
     * Save pairwise comparisons to database.
     */
    public void savePairwiseComparisons(List<PairwiseComparison> comparisons) throws SQLException {
        pairwiseRepository.upsertAll(comparisons);
    }

    /**
     * Get all saved pairwise comparisons.
     */
    public List<PairwiseComparison> getPairwiseComparisons() throws SQLException {
        return pairwiseRepository.findAll();
    }

    /**
     * Execute AHP calculation.
     * 
     * @return AHPResult containing weights, CR, and intermediate matrices
     * @throws IllegalStateException if criteria count < 2
     */
    public AHPResult calculate() throws SQLException {
        List<Criteria> criteriaList = criteriaRepository.findAll();
        int n = criteriaList.size();

        if (n < 2) {
            throw new IllegalStateException("Minimal 2 kriteria untuk perhitungan AHP");
        }

        // Build criteria ID index map
        List<Integer> criteriaIds = new ArrayList<>();
        for (Criteria c : criteriaList) {
            criteriaIds.add(c.getId());
        }

        // Step 1: Build pairwise comparison matrix
        double[][] matrix = buildPairwiseMatrix(criteriaIds, n);

        // Step 2: Normalize the matrix
        double[][] normalized = normalizeMatrix(matrix, n);

        // Step 3: Calculate priority vector (weights)
        double[] priorityVector = calculatePriorityVector(normalized, n);

        // Step 4: Calculate Consistency
        double lambdaMax = calculateLambdaMax(matrix, priorityVector, n);
        double ci = (n <= 1) ? 0 : (lambdaMax - n) / (n - 1);
        double ri = (n <= 15) ? RI[n - 1] : 1.59;
        double cr = (ri == 0) ? 0 : ci / ri;

        // Build result
        AHPResult result = new AHPResult();
        result.setPairwiseMatrix(matrix);
        result.setNormalizedMatrix(normalized);
        result.setPriorityVector(priorityVector);
        result.setLambdaMax(lambdaMax);
        result.setConsistencyIndex(ci);
        result.setConsistencyRatio(cr);

        // Map weights to criteria IDs
        Map<Integer, Double> weights = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            weights.put(criteriaIds.get(i), priorityVector[i]);
        }
        result.setWeights(weights);

        // Save weights if consistent
        if (result.isConsistent()) {
            resultRepository.saveAHPWeights(weights, cr);
        }

        return result;
    }

    /**
     * Build the pairwise comparison matrix from stored comparisons.
     * Diagonal = 1, reciprocal values for lower triangle.
     */
    private double[][] buildPairwiseMatrix(List<Integer> criteriaIds, int n) throws SQLException {
        double[][] matrix = new double[n][n];

        // Initialize diagonal to 1
        for (int i = 0; i < n; i++) {
            matrix[i][i] = 1.0;
        }

        // Fill from database
        List<PairwiseComparison> comparisons = pairwiseRepository.findAll();
        for (PairwiseComparison pc : comparisons) {
            int i = criteriaIds.indexOf(pc.getKriteriaId1());
            int j = criteriaIds.indexOf(pc.getKriteriaId2());
            if (i >= 0 && j >= 0) {
                matrix[i][j] = pc.getNilai();
                matrix[j][i] = 1.0 / pc.getNilai(); // reciprocal
            }
        }

        return matrix;
    }

    /**
     * Normalize the pairwise matrix by dividing each element by its column sum.
     */
    private double[][] normalizeMatrix(double[][] matrix, int n) {
        double[][] normalized = new double[n][n];

        // Calculate column sums
        double[] colSums = new double[n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                colSums[j] += matrix[i][j];
            }
        }

        // Normalize
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                normalized[i][j] = (colSums[j] != 0) ? matrix[i][j] / colSums[j] : 0;
            }
        }

        return normalized;
    }

    /**
     * Calculate the priority vector (weights) as the row averages of the normalized matrix.
     */
    private double[] calculatePriorityVector(double[][] normalized, int n) {
        double[] priority = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int j = 0; j < n; j++) {
                sum += normalized[i][j];
            }
            priority[i] = sum / n;
        }
        return priority;
    }

    /**
     * Calculate lambda max for consistency checking.
     * λmax = sum of (column sum * weight) for each criterion.
     */
    private double calculateLambdaMax(double[][] matrix, double[] weights, int n) {
        double lambdaMax = 0;

        // Calculate Aw (matrix * weight vector)
        double[] aw = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                aw[i] += matrix[i][j] * weights[j];
            }
        }

        // λmax = average of (Aw[i] / w[i])
        for (int i = 0; i < n; i++) {
            if (weights[i] != 0) {
                lambdaMax += aw[i] / weights[i];
            }
        }
        lambdaMax /= n;

        return lambdaMax;
    }

    /**
     * Check if AHP weights have already been calculated and saved.
     */
    public boolean hasWeights() throws SQLException {
        return resultRepository.hasWeights();
    }

    /**
     * Get the saved consistency ratio.
     */
    public double getSavedConsistencyRatio() throws SQLException {
        return resultRepository.getConsistencyRatio();
    }

    /**
     * Get the AHP intensity scale options for the UI.
     */
    public static Map<String, Double> getIntensityScale() {
        Map<String, Double> scale = new LinkedHashMap<>();
        scale.put("1 - Sama Penting", 1.0);
        scale.put("2 - Mendekati Sedikit Lebih Penting", 2.0);
        scale.put("3 - Sedikit Lebih Penting", 3.0);
        scale.put("4 - Mendekati Lebih Penting", 4.0);
        scale.put("5 - Lebih Penting", 5.0);
        scale.put("6 - Mendekati Sangat Penting", 6.0);
        scale.put("7 - Sangat Penting", 7.0);
        scale.put("8 - Mendekati Mutlak Penting", 8.0);
        scale.put("9 - Mutlak Penting", 9.0);
        return scale;
    }
}
