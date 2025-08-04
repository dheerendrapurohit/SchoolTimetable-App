package com.example.school.entity;

import java.io.Serializable;
import java.util.List;

public class Teacher implements Serializable {
    private Long id;
    private String name;
    private List<String> availablePeriods;
    private List<SubjectClassPair> subjectsAndClasses;

    public Teacher() {}

    public Teacher(Long id, String name, List<String> availablePeriods, List<SubjectClassPair> subjectsAndClasses) {
        this.id = id;
        this.name = name;
        this.availablePeriods = availablePeriods;
        this.subjectsAndClasses = subjectsAndClasses;
    }

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

    public List<SubjectClassPair> getSubjectsAndClasses() {
        return subjectsAndClasses;
    }

    public void setSubjectsAndClasses(List<SubjectClassPair> subjectsAndClasses) {
        this.subjectsAndClasses = subjectsAndClasses;
    }
}
