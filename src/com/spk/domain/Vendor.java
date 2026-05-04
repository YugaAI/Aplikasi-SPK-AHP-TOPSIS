package com.spk.domain;

/**
 * Entity representing a vendor (alternative) in the decision process.
 */
public class Vendor {
    private int id;
    private String namaVendor;
    private String deskripsi;

    public Vendor() {}

    public Vendor(int id, String namaVendor, String deskripsi) {
        this.id = id;
        this.namaVendor = namaVendor;
        this.deskripsi = deskripsi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaVendor() { return namaVendor; }
    public void setNamaVendor(String namaVendor) { this.namaVendor = namaVendor; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    @Override
    public String toString() {
        return namaVendor;
    }
}
