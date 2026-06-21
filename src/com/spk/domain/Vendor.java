package com.spk.domain;

/**
 * Entity representing a vendor (alternative) in the decision process.
 */
public class Vendor {
    private int id;
    private String namaVendor;
    private String alamat;

    public Vendor() {}

    public Vendor(int id, String namaVendor, String alamat) {
        this.id = id;
        this.namaVendor = namaVendor;
        this.alamat = alamat;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaVendor() { return namaVendor; }
    public void setNamaVendor(String namaVendor) { this.namaVendor = namaVendor; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    @Override
    public String toString() {
        return namaVendor;
    }
}