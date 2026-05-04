package com.spk.domain;

/**
 * Entity representing a single vendor's TOPSIS result.
 */
public class TOPSISResult {
    private int vendorId;
    private String vendorName;
    private double skorPreferensi;
    private int ranking;

    // Intermediate values for display
    private double jarakIdealPositif;
    private double jarakIdealNegatif;

    public TOPSISResult() {}

    public TOPSISResult(int vendorId, String vendorName, double skorPreferensi, int ranking) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.skorPreferensi = skorPreferensi;
        this.ranking = ranking;
    }

    public int getVendorId() { return vendorId; }
    public void setVendorId(int vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public double getSkorPreferensi() { return skorPreferensi; }
    public void setSkorPreferensi(double skorPreferensi) { this.skorPreferensi = skorPreferensi; }

    public int getRanking() { return ranking; }
    public void setRanking(int ranking) { this.ranking = ranking; }

    public double getJarakIdealPositif() { return jarakIdealPositif; }
    public void setJarakIdealPositif(double jarakIdealPositif) { this.jarakIdealPositif = jarakIdealPositif; }

    public double getJarakIdealNegatif() { return jarakIdealNegatif; }
    public void setJarakIdealNegatif(double jarakIdealNegatif) { this.jarakIdealNegatif = jarakIdealNegatif; }
}
