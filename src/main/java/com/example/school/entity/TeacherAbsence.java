package com.example.school.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAbsence {
    private Long id;
    private String name;
    private LocalDate date;
    private String day;

    public void setDate(LocalDate date) {
        this.date = date;
        this.day = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH); // auto-set day
    }
}
