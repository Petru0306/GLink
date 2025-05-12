package com.greenlink.greenlink.dto;

import java.util.List;

public class RecyclingPointDto {
    private Long id;               // ID-ul punctului de reciclare
    private String name;           // Numele punctului
    private String address;        // Adresa punctului
    private List<String> materialsAccepted; // Materialele acceptate (ex.: plastic, hârtie)

    // Constructori
    public RecyclingPointDto() {
    }

    public RecyclingPointDto(Long id, String name, String address, List<String> materialsAccepted) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.materialsAccepted = materialsAccepted;
    }

    // Getteri și setteri
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getMaterialsAccepted() {
        return materialsAccepted;
    }

    public void setMaterialsAccepted(List<String> materialsAccepted) {
        this.materialsAccepted = materialsAccepted;
    }
}
