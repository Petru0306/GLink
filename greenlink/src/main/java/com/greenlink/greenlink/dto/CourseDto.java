package com.greenlink.greenlink.dto;

public class CourseDto {
    private Long id;              // ID-ul cursului
    private String title;         // Titlul cursului
    private String description;   // Descrierea cursului
    private int duration;         // Durata cursului (în minute)

    // Constructori
    public CourseDto() {
    }

    public CourseDto(Long id, String title, String description, int duration) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
    }

    // Getteri și setteri
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
