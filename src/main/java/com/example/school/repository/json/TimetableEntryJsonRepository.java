package com.example.school.repository.json;

import com.example.school.entity.TimetableEntry;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TimetableEntryJsonRepository extends JsonRepository<TimetableEntry> {

    public TimetableEntryJsonRepository() {
        super("timetable_entries.json", TimetableEntry.class);
    }

    public List<TimetableEntry> findByDate(LocalDate date) {
        return findAll().stream()
                .filter(entry -> date.equals(entry.getDate()))
                .collect(Collectors.toList());
    }

    public List<TimetableEntry> findByDay(String day) {
        return findAll().stream()
                .filter(entry -> entry.getDay() != null &&
                        day.equalsIgnoreCase(entry.getDay()))
                .collect(Collectors.toList());
    }

    public List<TimetableEntry> findByClassroomName(String className) {
        return findAll().stream()
                .filter(entry -> entry.getClassroom() != null &&
                        className.equalsIgnoreCase(entry.getClassroom().getName()))
                .collect(Collectors.toList());
    }

    public List<TimetableEntry> findByTeacherName(String teacherName) {
        return findAll().stream()
                .filter(entry -> entry.getTeacher() != null &&
                        teacherName.equalsIgnoreCase(entry.getTeacher().getName()))
                .collect(Collectors.toList());
    }

    public List<TimetableEntry> findByTeacherNameAndDate(String teacherName, LocalDate date) {
        return findAll().stream()
                .filter(entry -> entry.getTeacher() != null &&
                        teacherName.equalsIgnoreCase(entry.getTeacher().getName()) &&
                        date.equals(entry.getDate()))
                .collect(Collectors.toList());
    }

}
