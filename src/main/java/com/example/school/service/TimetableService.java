package com.example.school.service;

import com.example.school.entity.TimetableEntry;

import java.time.LocalDate;
import java.util.List;

public interface TimetableService {

    List<TimetableEntry> getAllEntries();

    TimetableEntry saveEntry(TimetableEntry entry);

    void generateTimetableForWeek();

    void generateTimetableBetweenDates(LocalDate startDate, LocalDate endDate);

    void generateTimetableForSingleDay(LocalDate date);

    void handleTeacherAbsence(String teacherName, LocalDate date);

    void handleTeacherAbsenceForPeriods(String teacherName, LocalDate date, List<String> periodsToReplace);

    void exportToExcel(List<TimetableEntry> entries, LocalDate baseDate);
}
