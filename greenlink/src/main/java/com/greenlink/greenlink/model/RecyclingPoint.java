package com.greenlink.greenlink.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class RecyclingPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @ElementCollection
    @CollectionTable(name = "materials_accepted", joinColumns = @JoinColumn(name = "recycling_point_id"))
    @Column(name = "material")
    private List<String> materialsAccepted;

    @Column(length = 1000)
    private String description;

    @Column
    private String schedule;

    @Column(name = "contact_phone")
    private String contactPhone;

    // Constructors
    public RecyclingPoint() {
    }

    public RecyclingPoint(String name, String address, Double latitude, Double longitude, List<String> materialsAccepted) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.materialsAccepted = materialsAccepted;
    }

    // Getters and Setters
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
