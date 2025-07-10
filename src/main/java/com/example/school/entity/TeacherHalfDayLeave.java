package com.example.school.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class TeacherHalfDayLeave {

    private Long id;
    private String name;
    private String session;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    //  Required: Default constructor
    public TeacherHalfDayLeave() {
    }


    public TeacherHalfDayLeave(Long id, String name, String session, LocalDate date) {
        this.id = id;
        this.name = name;
        this.session = session;
        this.date = date;
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

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "TeacherHalfDayLeave{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", session='" + session + '\'' +
                ", date=" + date +
                '}';
    }
}
