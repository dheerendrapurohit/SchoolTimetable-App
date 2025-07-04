package com.example.school.entity;

import java.io.Serializable;
import java.util.List;

public class Teacher implements Serializable {
    private Long id;
    private String name;
    private List<String> availablePeriods;
    private List<String> subjects;

    // Getters and setters
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

    public List<String> getAvailablePeriods() {
        return availablePeriods;
    }

    public void setAvailablePeriods(List<String> availablePeriods) {
        this.availablePeriods = availablePeriods;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }
}
