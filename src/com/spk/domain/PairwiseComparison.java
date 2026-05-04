package com.spk.domain;

/**
 * Entity representing a pairwise comparison between two criteria in AHP.
 */
public class PairwiseComparison {
    private int id;
    private int kriteriaId1;
    private int kriteriaId2;
    private double nilai;

    public PairwiseComparison() {}

    public PairwiseComparison(int kriteriaId1, int kriteriaId2, double nilai) {
        this.kriteriaId1 = kriteriaId1;
        this.kriteriaId2 = kriteriaId2;
        this.nilai = nilai;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getKriteriaId1() { return kriteriaId1; }
    public void setKriteriaId1(int kriteriaId1) { this.kriteriaId1 = kriteriaId1; }

    public int getKriteriaId2() { return kriteriaId2; }
    public void setKriteriaId2(int kriteriaId2) { this.kriteriaId2 = kriteriaId2; }

    public double getNilai() { return nilai; }
    public void setNilai(double nilai) { this.nilai = nilai; }
}
