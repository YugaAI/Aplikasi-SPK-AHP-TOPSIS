package com.spk.domain;

/**
 * Entity representing a decision criterion.
 * Type can be "benefit" (higher is better) or "cost" (lower is better).
 */
public class Criteria {
    private int id;
    private String namaKriteria;
    private String tipeKriteria; // "benefit" or "cost"

    public Criteria() {}

    public Criteria(int id, String namaKriteria, String tipeKriteria) {
        this.id = id;
        this.namaKriteria = namaKriteria;
        this.tipeKriteria = tipeKriteria;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaKriteria() { return namaKriteria; }
    public void setNamaKriteria(String namaKriteria) { this.namaKriteria = namaKriteria; }

    public String getTipeKriteria() { return tipeKriteria; }
    public void setTipeKriteria(String tipeKriteria) { this.tipeKriteria = tipeKriteria; }

    public boolean isBenefit() {
        return "benefit".equalsIgnoreCase(tipeKriteria);
    }

    @Override
    public String toString() {
        return namaKriteria + " (" + tipeKriteria + ")";
    }
}
