package com.example.school.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class TeacherHalfDayLeave {

    private Long id;
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private List<String> periods;  // ✅ REPLACED session with this

    public TeacherHalfDayLeave() {
    }

    public TeacherHalfDayLeave(Long id, String name, LocalDate date, List<String> periods) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.periods = periods;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<String> getPeriods() {
        return periods;
    }

    public void setPeriods(List<String> periods) {
        this.periods = periods;
    }

    @Override
    public String toString() {
        return "TeacherHalfDayLeave{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", periods=" + periods +
                '}';
    }
}
