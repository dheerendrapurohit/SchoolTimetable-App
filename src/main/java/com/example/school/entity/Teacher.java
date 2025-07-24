package com.example.school.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Teacher implements Serializable {
    private Long id;
    private String name;
    private List<String> availablePeriods;
    private List<String> subjects;
    private List<String> availableClasses;
    private Map<String, List<String>> subjectClassMap;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public Map<String, List<String>> getSubjectClassMap() {
        return subjectClassMap;
    }

    public void setSubjectClassMap(Map<String, List<String>> subjectClassMap) {
        this.subjectClassMap = subjectClassMap;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<String> getAvailableClasses() {
        return availableClasses;
    }

    public void setAvailableClasses(List<String> availableClasses) {
        this.availableClasses = availableClasses;
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