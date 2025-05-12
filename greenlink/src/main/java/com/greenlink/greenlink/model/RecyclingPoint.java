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

    @ElementCollection
    @CollectionTable(name = "materials_accepted", joinColumns = @JoinColumn(name = "recycling_point_id"))
    @Column(name = "material")
    private List<String> materialsAccepted;

    // Constructori
    public RecyclingPoint() {
    }

    public RecyclingPoint(Long id, String name, String address, List<String> materialsAccepted) {
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
