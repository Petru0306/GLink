package com.greenlink.greenlink.dto;

import java.util.List;

public class RecyclingPointDto {
    private Long id;               // ID-ul punctului de reciclare
    private String name;           // Numele punctului
    private String address;        // Adresa punctului
    private Double latitude;
    private Double longitude;
    private List<String> materialsAccepted; // Materialele acceptate (ex.: plastic, hârtie)
    private String description;
    private String schedule;
    private String contactPhone;

    // Constructori
    public RecyclingPointDto() {
    }

    public RecyclingPointDto(Long id, String name, String address, Double latitude, Double longitude,
                           List<String> materialsAccepted, String description, String schedule, String contactPhone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.materialsAccepted = materialsAccepted;
        this.description = description;
        this.schedule = schedule;
        this.contactPhone = contactPhone;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<String> getMaterialsAccepted() {
        return materialsAccepted;
    }

    public void setMaterialsAccepted(List<String> materialsAccepted) {
        this.materialsAccepted = materialsAccepted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
