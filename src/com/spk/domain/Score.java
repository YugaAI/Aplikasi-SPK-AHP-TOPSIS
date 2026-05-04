package com.spk.domain;

/**
 * Entity representing a vendor's score for a specific criterion.
 */
public class Score {
    private int id;
    private int vendorId;
    private int kriteriaId;
    private double nilai;

    // Transient fields for display
    private String vendorName;
    private String kriteriaName;

    public Score() {}

    public Score(int vendorId, int kriteriaId, double nilai) {
        this.vendorId = vendorId;
        this.kriteriaId = kriteriaId;
        this.nilai = nilai;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVendorId() { return vendorId; }
    public void setVendorId(int vendorId) { this.vendorId = vendorId; }

    public int getKriteriaId() { return kriteriaId; }
    public void setKriteriaId(int kriteriaId) { this.kriteriaId = kriteriaId; }

    public double getNilai() { return nilai; }
    public void setNilai(double nilai) { this.nilai = nilai; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getKriteriaName() { return kriteriaName; }
    public void setKriteriaName(String kriteriaName) { this.kriteriaName = kriteriaName; }
}
