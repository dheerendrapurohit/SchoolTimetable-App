package com.example.school.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class TimetableEntry implements Serializable {
    private Long id;
    private LocalDate date;
    private String day;
    private Period period;
    private Classroom classroom;
    private Subject subject;
    private Teacher teacher;

    public void setDate(LocalDate date) {
        this.date = date;
        this.day = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public LocalDate getDate() {
        return date;
    }


    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
}
