package com.example.school.service;

import com.example.school.config.DataLoader;
import com.example.school.entity.*;
import com.example.school.repository.json.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
public class TimetableService {
    private static final Logger logger = LoggerFactory.getLogger(TimetableService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ClassroomJsonRepository classroomRepository;
    private final TimetableEntryJsonRepository repository;
    private final SubjectJsonRepository subjectRepository;
    private final PeriodJsonRepository periodRepository;
    private final TeacherJsonRepository teacherRepository;

    public TimetableService(
            ClassroomJsonRepository classroomRepository,
            TimetableEntryJsonRepository repository,
            SubjectJsonRepository subjectRepository,
            PeriodJsonRepository periodRepository,
            TeacherJsonRepository teacherRepository) {

        this.classroomRepository = classroomRepository;
        this.repository = repository;
        this.subjectRepository = subjectRepository;
        this.periodRepository = periodRepository;
        this.teacherRepository = teacherRepository;
    }

    public List<TimetableEntry> getAllEntries() {
        return repository.findAll();
    }

    public TimetableEntry saveEntry(TimetableEntry entry) {
        return repository.save(entry);
    }

    public void generateTimetableForWeek() {
        repository.clear(); // Clear JSON file
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        List<TimetableEntry> weeklyEntries = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            List<TimetableEntry> dailyEntries = generateTimetableForDate(monday.plusDays(i));
            weeklyEntries.addAll(dailyEntries);
        }

        // Save to JSON
        repository.saveAll(weeklyEntries);


        // Export to Excel
        exportToExcel(weeklyEntries, monday);
    }

    public void generateTimetableBetweenDates(LocalDate startDate, LocalDate endDate) {
        repository.clear(); // Clear existing timetable

        List<TimetableEntry> allEntries = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                List<TimetableEntry> dailyEntries = generateTimetableForDate(current);
                allEntries.addAll(dailyEntries);
            }
            current = current.plusDays(1);
        }

        repository.saveAll(allEntries);
        exportToExcel(allEntries, startDate);
    }

    public void generateTimetableForSingleDay(LocalDate date) {
        System.out.println("Generating timetable for: " + date);

        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            System.out.println("Skipping Sunday.");
            return;
        }

        List<TimetableEntry> entries = generateTimetableForDate(date);
        repository.saveAll(entries);
        exportToExcel(entries, date);

        System.out.println("Timetable generated and saved for: " + date);
    }




    // Extracted logic to generate one day's timetable
    public List<TimetableEntry> generateTimetableForDate(LocalDate date) {
        List<TimetableEntry> dayEntries = new ArrayList<>();
        List<Classroom> classrooms = classroomRepository.findAll();
        List<Subject> subjects = subjectRepository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();
        List<Period> periods = periodRepository.findAll();

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int periodLimit = (dayOfWeek == DayOfWeek.SATURDAY) ? 4 : 7;

        Set<String> yogaAssignedClassrooms = new HashSet<>();
        for (Classroom classroom : classrooms) {
            Map<String, Integer> subjectCount = new HashMap<>();
            Set<String> uniqueSubjectsToday = new HashSet<>();

            for (int i = 1; i <= periodLimit; i++) {
                String periodName = "P" + i;
                Period period = periods.stream().filter(p -> p.getName().equals(periodName)).findFirst().orElse(null);
                if (period == null) continue;

                Subject subject = findSubjectForDay(subjects, subjectCount, uniqueSubjectsToday, yogaAssignedClassrooms, classroom.getName());
                if (subject == null) continue;

                Teacher teacher = findAvailableTeacher(teachers, subject.getName(), period.getName());
                if (teacher == null) continue;

                TimetableEntry entry = new TimetableEntry();
                entry.setDate(date);
                entry.setDay(dayOfWeek.toString());
                entry.setPeriod(period);
                entry.setClassroom(classroom);
                entry.setSubject(subject);
                entry.setTeacher(teacher);

                repository.save(entry);
                dayEntries.add(entry);

                subjectCount.put(subject.getName(), subjectCount.getOrDefault(subject.getName(), 0) + 1);
                uniqueSubjectsToday.add(subject.getName());

                if (subject.getName().equalsIgnoreCase("Yoga")) {
                    yogaAssignedClassrooms.add(classroom.getName());
                }
            }
        }

        return dayEntries;
    }

    private Subject findSubjectForDay(List<Subject> subjects,
                                      Map<String, Integer> subjectCount,
                                      Set<String> uniqueSubjectsToday,
                                      Set<String> yogaAssignedClassrooms,
                                      String classroomName) {

        for (Subject subject : subjects) {
            String name = subject.getName();

            if (name.equalsIgnoreCase("Yoga")) {
                if (yogaAssignedClassrooms.contains(classroomName)) continue;
                return subject;
            }

            if (uniqueSubjectsToday.size() >= 4 && !uniqueSubjectsToday.contains(name)) {
                continue;
            }

            if (subjectCount.getOrDefault(name, 0) < 5) {
                return subject;
            }
        }

        return null;
    }

    private Teacher findAvailableTeacher(List<Teacher> teachers, String subject, String periodName,
                                         Map<String, Integer> teacherPeriodCount, DayOfWeek dayOfWeek) {
        for (Teacher t : teachers) {
            if (t.getSubjects().contains(subject) && t.getAvailablePeriods().contains(periodName)) {
                int count = teacherPeriodCount.getOrDefault(t.getId(), 0);
                if (dayOfWeek != DayOfWeek.SATURDAY && count >= 4) continue;
                if (dayOfWeek == DayOfWeek.SATURDAY && count >= 2) continue; // Optional: limit Saturday to 2

                return t;
            }
        }
        return null;
    }
    private Teacher findAvailableTeacher(List<Teacher> teachers, String subject, String periodName) {
        for (Teacher t : teachers) {
            if (t.getSubjects().contains(subject) && t.getAvailablePeriods().contains(periodName)) {
                return t;
            }
        }
        return null;
    }


    public void handleTeacherAbsence(String teacherName, LocalDate date) {
        List<TimetableEntry> allEntries = repository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();

        Map<String, Integer> teacherPeriodCount = new HashMap<>();

        for (TimetableEntry entry : allEntries) {
            if (entry.getTeacher().getName().equalsIgnoreCase(teacherName)
                    && entry.getDate().equals(date)) {

                String subject = entry.getSubject().getName();
                String period = entry.getPeriod().getName();

                Teacher substitute = findAvailableTeacher(
                        teachers, subject, period, teacherPeriodCount, entry.getDate().getDayOfWeek());

                if (substitute != null) {
                    entry.setTeacher(substitute);

                    // Update the substitute's period count
                    String id = substitute.getId().toString(); // convert Long to String
                    teacherPeriodCount.put(id, teacherPeriodCount.getOrDefault(id, 0) + 1);

                } else {
                    entry.setTeacher(null);
                }
            }
        }

        repository.saveAll(allEntries);
    }



    public void handleTeacherAbsence(String teacherName, String day) {
        List<TimetableEntry> allEntries = repository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();

        for (TimetableEntry entry : allEntries) {
            if (entry.getTeacher().getName().equalsIgnoreCase(teacherName)
                    && entry.getDay().equalsIgnoreCase(day)) {

                String subject = entry.getSubject().getName();
                String period = entry.getPeriod().getName();

                Teacher substitute = findAvailableTeacher(teachers, subject, period);
                entry.setTeacher(substitute != null ? substitute : null);
            }
        }

        repository.saveAll(allEntries);
    }

    public void handleTeacherAbsenceForPeriods(String teacherName, LocalDate date, List<String> periodsToReplace) {
        List<TimetableEntry> allEntries = repository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();

        for (TimetableEntry entry : allEntries) {
            if (entry.getTeacher().getName().equalsIgnoreCase(teacherName)
                    && entry.getDate().equals(date)
                    && periodsToReplace.contains(entry.getPeriod().getName())) {

                String subject = entry.getSubject().getName();
                String period = entry.getPeriod().getName();

                Teacher substitute = findAvailableTeacher(teachers, subject, period);
                if (substitute != null) {
                    entry.setTeacher(substitute);
                    logger.info("Substituted teacher '{}' with '{}' for period '{}' on {}",
                            teacherName, substitute.getName(), period, date);
                } else {
                    entry.setTeacher(null);
                    logger.warn("No substitute available for teacher '{}' on period '{}' for {}",
                            teacherName, period, date);
                }
            }
        }

        repository.saveAll(allEntries);
    }



    public void exportToExcel(List<TimetableEntry> entries, LocalDate monday) {
        String fileName = "timetable_" + monday + ".xlsx";
        File exportDir = new File("exports");

        if (exportDir.exists() && !exportDir.isDirectory()) {
            logger.error("'exports' exists as a file, not a directory. Please delete or rename it.");
            return;
        }

        if (!exportDir.exists() && !exportDir.mkdirs()) {
            logger.error("Failed to create exports directory");
            return;
        }

        File file = new File(exportDir, fileName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Timetable");

            String[] columns = {"Date", "Day", "Classroom", "Period", "Subject", "Teacher"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowNum = 1;
            for (TimetableEntry entry : entries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getDate().toString());
                row.createCell(1).setCellValue(entry.getDay());
                row.createCell(2).setCellValue(entry.getClassroom().getName());
                row.createCell(3).setCellValue(entry.getPeriod().getName());
                row.createCell(4).setCellValue(entry.getSubject().getName());
                row.createCell(5).setCellValue(entry.getTeacher().getName());
            }

            //  Auto-size columns after data is written
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            logger.info(" Exported timetable to Excel: {}", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error(" Failed to export Excel", e);
        }
    }


}