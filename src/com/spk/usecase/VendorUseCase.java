package com.spk.usecase;

import java.sql.SQLException;
import java.util.List;

import com.spk.domain.Vendor;
import com.spk.repository.VendorRepository;

/**
 * Use case for vendor (alternative) management.
 */
public class VendorUseCase {
    private final VendorRepository vendorRepository;

    public VendorUseCase() {
        this.vendorRepository = new VendorRepository();
    }

    public List<Vendor> getAllVendors() throws SQLException {
        return vendorRepository.findAll();
    }

    public Vendor getVendorById(int id) throws SQLException {
        return vendorRepository.findById(id);
    }

    public void createVendor(String nama, String alamat) throws SQLException {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama vendor tidak boleh kosong");
        }

        Vendor vendor = new Vendor();
        vendor.setNamaVendor(nama.trim());
        vendor.setAlamat(alamat != null ? alamat.trim() : "");
        vendorRepository.insert(vendor);
    }

    public void updateVendor(int id, String nama, String alamat) throws SQLException {
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama vendor tidak boleh kosong");
        }

        Vendor vendor = vendorRepository.findById(id);
        if (vendor == null) {
            throw new IllegalArgumentException("Vendor tidak ditemukan");
        }
        vendor.setNamaVendor(nama.trim());
        vendor.setAlamat(alamat != null ? alamat.trim() : "");
        vendorRepository.update(vendor);
    }

    public void deleteVendor(int id) throws SQLException {
        vendorRepository.delete(id);
    }

    public int getVendorCount() throws SQLException {
        return vendorRepository.count();
    }
}
