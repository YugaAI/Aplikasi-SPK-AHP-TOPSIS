package com.spk.usecase;

import com.spk.domain.Criteria;
import com.spk.domain.TOPSISResult;
import com.spk.domain.Vendor;
import com.spk.repository.*;

import java.sql.SQLException;
import java.util.*;

/**
 * Use case for TOPSIS (Technique for Order of Preference by Similarity to Ideal Solution) calculation.
 * 
 * TOPSIS Steps:
 * 1. Build decision matrix (vendors x criteria)
 * 2. Normalize the decision matrix (vector normalization)
 * 3. Build weighted normalized matrix (multiply by AHP weights)
 * 4. Determine ideal positive (A+) and ideal negative (A-) solutions
 * 5. Calculate distance to A+ and A- for each alternative
 * 6. Calculate preference score (closeness coefficient)
 * 7. Rank alternatives by preference score (descending)
 */
public class CalculateTOPSISUseCase {
    private final VendorRepository vendorRepository;
    private final CriteriaRepository criteriaRepository;
    private final ScoreRepository scoreRepository;
    private final ResultRepository resultRepository;

    // Intermediate calculation data for display
    private double[][] normalizedMatrix;
    private double[][] weightedNormalizedMatrix;
    private double[] idealPositive;
    private double[] idealNegative;

    public CalculateTOPSISUseCase() {
        this.vendorRepository = new VendorRepository();
        this.criteriaRepository = new CriteriaRepository();
        this.scoreRepository = new ScoreRepository();
        this.resultRepository = new ResultRepository();
    }

    /**
     * Execute TOPSIS calculation.
     * 
     * @return List of TOPSISResult sorted by ranking
     * @throws IllegalStateException if prerequisites are not met
     */
    public List<TOPSISResult> calculate() throws SQLException {
        // Validate prerequisites
        if (!resultRepository.hasWeights()) {
            throw new IllegalStateException("Bobot AHP belum dihitung. Lakukan perhitungan AHP terlebih dahulu.");
        }

        List<Vendor> vendors = vendorRepository.findAll();
        List<Criteria> criteriaList = criteriaRepository.findAll();

        if (vendors.size() < 2) {
            throw new IllegalStateException("Minimal 2 vendor untuk perhitungan TOPSIS");
        }
        if (criteriaList.size() < 2) {
            throw new IllegalStateException("Minimal 2 kriteria untuk perhitungan TOPSIS");
        }

        int m = vendors.size();    // number of alternatives
        int n = criteriaList.size(); // number of criteria

        List<Integer> vendorIds = new ArrayList<>();
        for (Vendor v : vendors) vendorIds.add(v.getId());

        List<Integer> criteriaIds = new ArrayList<>();
        for (Criteria c : criteriaList) criteriaIds.add(c.getId());

        // Step 1: Build decision matrix
        double[][] decisionMatrix = scoreRepository.getScoreMatrix(vendorIds, criteriaIds);

        // Validate: no zeros or missing values
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (decisionMatrix[i][j] == 0) {
                    throw new IllegalStateException(
                            "Nilai kosong ditemukan untuk vendor '" + vendors.get(i).getNamaVendor() +
                                    "' pada kriteria '" + criteriaList.get(j).getNamaKriteria() + "'");
                }
            }
        }

        // Step 2: Vector normalization
        normalizedMatrix = vectorNormalize(decisionMatrix, m, n);

        // Step 3: Weighted normalized matrix
        double[] weights = new double[n];
        for (int j = 0; j < n; j++) {
            weights[j] = resultRepository.getWeight(criteriaIds.get(j));
        }
        weightedNormalizedMatrix = applyWeights(normalizedMatrix, weights, m, n);

        // Step 4: Determine ideal solutions
        idealPositive = new double[n];
        idealNegative = new double[n];

        for (int j = 0; j < n; j++) {
            double max = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY;
            for (int i = 0; i < m; i++) {
                if (weightedNormalizedMatrix[i][j] > max) max = weightedNormalizedMatrix[i][j];
                if (weightedNormalizedMatrix[i][j] < min) min = weightedNormalizedMatrix[i][j];
            }

            if (criteriaList.get(j).isBenefit()) {
                idealPositive[j] = max;  // A+ = max for benefit
                idealNegative[j] = min;  // A- = min for benefit
            } else {
                idealPositive[j] = min;  // A+ = min for cost
                idealNegative[j] = max;  // A- = max for cost
            }
        }

        // Step 5: Calculate distances
        double[] dPositive = new double[m]; // distance to ideal positive
        double[] dNegative = new double[m]; // distance to ideal negative

        for (int i = 0; i < m; i++) {
            double sumPos = 0, sumNeg = 0;
            for (int j = 0; j < n; j++) {
                sumPos += Math.pow(weightedNormalizedMatrix[i][j] - idealPositive[j], 2);
                sumNeg += Math.pow(weightedNormalizedMatrix[i][j] - idealNegative[j], 2);
            }
            dPositive[i] = Math.sqrt(sumPos);
            dNegative[i] = Math.sqrt(sumNeg);
        }

        // Step 6: Calculate preference scores
        List<TOPSISResult> results = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            double score = 0;
            if ((dPositive[i] + dNegative[i]) != 0) {
                score = dNegative[i] / (dPositive[i] + dNegative[i]);
            }

            TOPSISResult result = new TOPSISResult();
            result.setVendorId(vendorIds.get(i));
            result.setVendorName(vendors.get(i).getNamaVendor());
            result.setSkorPreferensi(score);
            result.setJarakIdealPositif(dPositive[i]);
            result.setJarakIdealNegatif(dNegative[i]);
            results.add(result);
        }

        // Step 7: Sort by preference score descending and assign ranking
        Collections.sort(results, new Comparator<TOPSISResult>() {
            @Override
            public int compare(TOPSISResult a, TOPSISResult b) {
                return Double.compare(b.getSkorPreferensi(), a.getSkorPreferensi());
            }
        });

        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRanking(i + 1);
        }

        // Save results to database
        resultRepository.saveTOPSISResults(results);

        return results;
    }

    /**
     * Vector normalization: r_ij = x_ij / sqrt(sum(x_ij^2))
     */
    private double[][] vectorNormalize(double[][] matrix, int m, int n) {
        double[][] normalized = new double[m][n];

        for (int j = 0; j < n; j++) {
            // Calculate column vector length
            double sumSquares = 0;
            for (int i = 0; i < m; i++) {
                sumSquares += matrix[i][j] * matrix[i][j];
            }
            double divisor = Math.sqrt(sumSquares);

            // Normalize
            for (int i = 0; i < m; i++) {
                normalized[i][j] = (divisor != 0) ? matrix[i][j] / divisor : 0;
            }
        }

        return normalized;
    }

    /**
     * Multiply normalized matrix by weight vector.
     */
    private double[][] applyWeights(double[][] normalized, double[] weights, int m, int n) {
        double[][] weighted = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                weighted[i][j] = normalized[i][j] * weights[j];
            }
        }
        return weighted;
    }

    /**
     * Get saved TOPSIS results.
     */
    public List<TOPSISResult> getSavedResults() throws SQLException {
        return resultRepository.getTOPSISResults();
    }

    /**
     * Check if TOPSIS results have been calculated.
     */
    public boolean hasResults() throws SQLException {
        return resultRepository.hasResults();
    }

    // Getters for intermediate matrices (for display in UI)
    public double[][] getNormalizedMatrix() { return normalizedMatrix; }
    public double[][] getWeightedNormalizedMatrix() { return weightedNormalizedMatrix; }
    public double[] getIdealPositive() { return idealPositive; }
    public double[] getIdealNegative() { return idealNegative; }
}
