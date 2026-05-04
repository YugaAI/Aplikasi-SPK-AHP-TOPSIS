package com.spk.usecase;

import com.spk.domain.Score;
import com.spk.domain.Criteria;
import com.spk.domain.Vendor;
import com.spk.repository.ScoreRepository;
import com.spk.repository.CriteriaRepository;
import com.spk.repository.VendorRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Use case for managing vendor scores against criteria.
 */
public class ScoreUseCase {
    private final ScoreRepository scoreRepository;
    private final VendorRepository vendorRepository;
    private final CriteriaRepository criteriaRepository;

    public ScoreUseCase() {
        this.scoreRepository = new ScoreRepository();
        this.vendorRepository = new VendorRepository();
        this.criteriaRepository = new CriteriaRepository();
    }

    public List<Score> getAllScores() throws SQLException {
        return scoreRepository.findAll();
    }

    public List<Score> getScoresByVendor(int vendorId) throws SQLException {
        return scoreRepository.findByVendor(vendorId);
    }

    /**
     * Save scores for a vendor. All criteria must have a score.
     */
    public void saveScores(int vendorId, List<Score> scores) throws SQLException {
        List<Criteria> allCriteria = criteriaRepository.findAll();

        // Validate: all criteria must have a value
        if (scores.size() != allCriteria.size()) {
            throw new IllegalArgumentException("Semua kriteria harus memiliki nilai");
        }

        for (Score s : scores) {
            if (s.getNilai() <= 0) {
                throw new IllegalArgumentException("Nilai tidak boleh kosong atau nol");
            }
            s.setVendorId(vendorId);
        }

        scoreRepository.upsertAll(scores);
    }

    /**
     * Check if all scores are complete (every vendor has score for every criteria).
     */
    public boolean isScoreComplete() throws SQLException {
        int vendorCount = vendorRepository.count();
        int criteriaCount = criteriaRepository.count();
        if (vendorCount == 0 || criteriaCount == 0) return false;
        return scoreRepository.isComplete(vendorCount, criteriaCount);
    }

    /**
     * Get score matrix for TOPSIS calculation.
     */
    public double[][] getScoreMatrix(List<Integer> vendorIds, List<Integer> criteriaIds) throws SQLException {
        return scoreRepository.getScoreMatrix(vendorIds, criteriaIds);
    }
}
