package com.spk.usecase;

import com.spk.domain.Criteria;
import com.spk.repository.CriteriaRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Use case for criteria management.
 */
public class CriteriaUseCase {
    private final CriteriaRepository criteriaRepository;

    public CriteriaUseCase() {
        this.criteriaRepository = new CriteriaRepository();
    }

    public List<Criteria> getAllCriteria() throws SQLException {
        return criteriaRepository.findAll();
    }

    public Criteria getCriteriaById(int id) throws SQLException {
        return criteriaRepository.findById(id);
    }

    public void createCriteria(String nama, String tipe) throws SQLException {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama kriteria tidak boleh kosong");
        }
        if (tipe == null || (!tipe.equals("benefit") && !tipe.equals("cost"))) {
            throw new IllegalArgumentException("Tipe kriteria harus 'benefit' atau 'cost'");
        }

        Criteria criteria = new Criteria();
        criteria.setNamaKriteria(nama.trim());
        criteria.setTipeKriteria(tipe);
        criteriaRepository.insert(criteria);
    }

    public void updateCriteria(int id, String nama, String tipe) throws SQLException {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama kriteria tidak boleh kosong");
        }
        if (tipe == null || (!tipe.equals("benefit") && !tipe.equals("cost"))) {
            throw new IllegalArgumentException("Tipe kriteria harus 'benefit' atau 'cost'");
        }

        Criteria criteria = criteriaRepository.findById(id);
        if (criteria == null) {
            throw new IllegalArgumentException("Kriteria tidak ditemukan");
        }
        criteria.setNamaKriteria(nama.trim());
        criteria.setTipeKriteria(tipe);
        criteriaRepository.update(criteria);
    }

    public void deleteCriteria(int id) throws SQLException {
        criteriaRepository.delete(id);
    }

    public int getCriteriaCount() throws SQLException {
        return criteriaRepository.count();
    }
}
