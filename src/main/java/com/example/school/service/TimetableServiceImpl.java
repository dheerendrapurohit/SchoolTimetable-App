package com.example.school.service;

import com.example.school.entity.*;
import com.example.school.repository.json.*;
import com.example.school.service.TimetableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimetableServiceImpl implements TimetableService {

    private static final Logger logger = LoggerFactory.getLogger(TimetableServiceImpl.class);

    private final TimetableEntryJsonRepository timetableEntryRepository;
    private final TeacherJsonRepository teacherRepository;
    private final SubjectJsonRepository subjectRepository;
    private final ClassroomJsonRepository classroomRepository;
    private final PeriodJsonRepository periodRepository;

    private static final int MAX_UNIQUE_SUBJECTS_PER_DAY = 4;
    private static final int MAX_SUBJECT_OCCURRENCE_PER_DAY = 5;
    private static final int MAX_TEACHER_PERIODS_WEEKDAY = 4;
    private static final int MAX_TEACHER_PERIODS_SATURDAY = 2;
    private static final Set<String> OPTIONAL_SUBJECTS = Set.of("Yoga");
    private static final int MAX_PERIODS_MON_FRI = 6;
    private static final int MAX_PERIODS_SAT = 4;

    public TimetableServiceImpl(TimetableEntryJsonRepository timetableEntryRepository,
                                TeacherJsonRepository teacherRepository,
                                SubjectJsonRepository subjectRepository,
                                ClassroomJsonRepository classroomRepository,
                                PeriodJsonRepository periodRepository) {
        this.timetableEntryRepository = timetableEntryRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.classroomRepository = classroomRepository;
        this.periodRepository = periodRepository;
    }

    @Override
    public void generateTimetableForSingleDay(LocalDate date) {
        List<Classroom> classrooms = classroomRepository.findAll();
        List<Period> periods = periodRepository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();
        List<Subject> subjects = subjectRepository.findAll();

        DayOfWeek day = date.getDayOfWeek();
        boolean isSaturday = day == DayOfWeek.SATURDAY;
        int maxTeacherPeriods = isSaturday ? MAX_TEACHER_PERIODS_SATURDAY : MAX_TEACHER_PERIODS_WEEKDAY;
        int maxPeriodsPerDay = isSaturday ? MAX_PERIODS_SAT : MAX_PERIODS_MON_FRI;

        List<Period> todaysPeriods = periods.stream()
                .sorted(Comparator.comparing(Period::getName))
                .limit(maxPeriodsPerDay)
                .toList();

        Map<Long, Integer> teacherPeriodCount = new HashMap<>();
        Map<String, Set<String>> optionalAssignedPerClass = new HashMap<>();

        for (Classroom classroom : classrooms) {
            Set<String> uniqueSubjects = new HashSet<>();
            Map<String, Integer> subjectUsage = new HashMap<>();
            int optionalAssigned = 0;

            for (Period period : todaysPeriods) {
                Subject selectedSubject = null;
                List<Subject> shuffledSubjects = new ArrayList<>(subjects);
                Collections.shuffle(shuffledSubjects);

                for (Subject s : shuffledSubjects) {
                    if (OPTIONAL_SUBJECTS.contains(s.getName())) {
                        if (optionalAssignedPerClass.getOrDefault(classroom.getName(), new HashSet<>()).contains(s.getName())) continue;
                        optionalAssigned++;
                    } else {
                        if (uniqueSubjects.size() >= MAX_UNIQUE_SUBJECTS_PER_DAY && !uniqueSubjects.contains(s.getName())) continue;
                        if (subjectUsage.getOrDefault(s.getName(), 0) >= MAX_SUBJECT_OCCURRENCE_PER_DAY) continue;
                    }
                    selectedSubject = s;
                    break;
                }

                if (selectedSubject == null) {
                    logger.warn("No subject available for {} {}", classroom.getName(), period.getName());
                    continue;
                }

                final Subject finalSubject = selectedSubject;
                List<Teacher> availableTeachers = teachers.stream()
                        .filter(t -> t.getSubjects().contains(finalSubject.getName()))
                        .filter(t -> t.getAvailableClasses().contains(classroom.getName()))
                        .filter(t -> t.getAvailablePeriods().contains(period.getName()))
                        .filter(t -> teacherPeriodCount.getOrDefault(t.getId(), 0) < maxTeacherPeriods)
                        .collect(Collectors.toList());

                if (availableTeachers.isEmpty()) {
                    logger.warn("No teacher available for {} {} {}", classroom.getName(), period.getName(), finalSubject.getName());
                    continue;
                }

                Teacher teacher = availableTeachers.get(new Random().nextInt(availableTeachers.size()));

                teacherPeriodCount.put(teacher.getId(), teacherPeriodCount.getOrDefault(teacher.getId(), 0) + 1);
                uniqueSubjects.add(selectedSubject.getName());
                subjectUsage.put(selectedSubject.getName(), subjectUsage.getOrDefault(selectedSubject.getName(), 0) + 1);
                if (OPTIONAL_SUBJECTS.contains(selectedSubject.getName())) {
                    optionalAssignedPerClass.computeIfAbsent(classroom.getName(), k -> new HashSet<>()).add(selectedSubject.getName());
                }

                TimetableEntry entry = new TimetableEntry();
                entry.setDate(date);
                entry.setDay(day.toString());
                entry.setClassroom(classroom);
                entry.setPeriod(period);
                entry.setSubject(selectedSubject);
                entry.setTeacher(teacher);

                timetableEntryRepository.save(entry);
            }
        }

        timetableEntryRepository.saveToFile();
        logger.info("Timetable generated for {}", date);
    }

    @Override
    public List<TimetableEntry> getAllEntries() {
        return timetableEntryRepository.findAll();
    }

    @Override
    public TimetableEntry saveEntry(TimetableEntry entry) {
        timetableEntryRepository.save(entry);
        timetableEntryRepository.saveToFile();
        return entry;
    }

    @Override
    public void generateTimetableForWeek() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        for (int i = 0; i < 6; i++) {
            generateTimetableForSingleDay(monday.plusDays(i));
        }
    }

    @Override
    public void generateTimetableBetweenDates(LocalDate startDate, LocalDate endDate) {
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            generateTimetableForSingleDay(date);
        }
    }

    @Override
    public void handleTeacherAbsence(String teacherName, LocalDate date) {
        List<TimetableEntry> entries = timetableEntryRepository.findByDate(date);
        List<Teacher> teachers = teacherRepository.findAll();

        for (TimetableEntry entry : entries) {
            if (entry.getTeacher().getName().equalsIgnoreCase(teacherName)) {
                Subject subject = entry.getSubject();
                String classroom = entry.getClassroom().getName();
                String period = entry.getPeriod().getName();

                // Find replacement
                Optional<Teacher> replacement = teachers.stream()
                        .filter(t -> !t.getName().equalsIgnoreCase(teacherName))
                        .filter(t -> t.getSubjects().contains(subject.getName()))
                        .filter(t -> t.getAvailableClasses().contains(classroom))
                        .filter(t -> t.getAvailablePeriods().contains(period))
                        .findAny();

                replacement.ifPresent(entry::setTeacher);
            }
        }

        timetableEntryRepository.saveToFile();
    }


    @Override
    public void handleTeacherAbsenceForPeriods(String teacherName, LocalDate date, List<String> periodsToReplace) {
        List<TimetableEntry> entries = timetableEntryRepository.findByDate(date);
        List<Teacher> teachers = teacherRepository.findAll();

        for (TimetableEntry entry : entries) {
            if (entry.getTeacher().getName().equalsIgnoreCase(teacherName) &&
                    periodsToReplace.contains(entry.getPeriod().getName())) {

                Subject subject = entry.getSubject();
                String classroom = entry.getClassroom().getName();
                String period = entry.getPeriod().getName();

                // Find replacement
                Optional<Teacher> replacement = teachers.stream()
                        .filter(t -> !t.getName().equalsIgnoreCase(teacherName))
                        .filter(t -> t.getSubjects().contains(subject.getName()))
                        .filter(t -> t.getAvailableClasses().contains(classroom))
                        .filter(t -> t.getAvailablePeriods().contains(period))
                        .findAny();

                replacement.ifPresent(entry::setTeacher);
            }
        }

        timetableEntryRepository.saveToFile();
    }


    @Override
    public void exportToExcel(List<TimetableEntry> entries, LocalDate baseDate) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Timetable");

        Row header = sheet.createRow(0);
        String[] columns = {"Date", "Day", "Class", "Period", "Subject", "Teacher"};
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

        String fileName = "timetable_" + baseDate + ".xlsx";
        try (FileOutputStream fileOut = new FileOutputStream(new File(fileName))) {
            workbook.write(fileOut);
            workbook.close();
        } catch (IOException e) {
            logger.error("Failed to export timetable to Excel", e);
        }
    }

}
