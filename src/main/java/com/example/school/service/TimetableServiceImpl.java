package com.example.school.service;

import com.example.school.entity.*;
import com.example.school.repository.json.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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


    private static final int MAX_TEACHER_PERIODS_WEEKDAY = 6;
    private static final int MAX_TEACHER_PERIODS_SATURDAY = 2;

    private static final int MAX_PERIODS_MON_FRI = 7;
    private static final int MAX_PERIODS_SAT = 4;

    private static final int MAX_WEEKLY_YOGA = 1;
    private static final int MAX_WEEKLY_CORE = 22;
    private static final int MAX_WEEKLY_LANG_SKILLS = 16;

    private final Map<String, Map<String, Integer>> weeklySubjectGroupCountPerClass = new HashMap<>();
    private final Map<String, Integer> yogaAssignedPerClass = new HashMap<>();

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
        List<Period> allPeriods = periodRepository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();
        List<Subject> subjects = subjectRepository.findAll();

        DayOfWeek day = date.getDayOfWeek();
        boolean isSaturday = (day == DayOfWeek.SATURDAY);
        int maxTeacherPeriods = isSaturday ? MAX_TEACHER_PERIODS_SATURDAY : MAX_TEACHER_PERIODS_WEEKDAY;
        int maxPeriodsToday = isSaturday ? MAX_PERIODS_SAT : MAX_PERIODS_MON_FRI;

        List<Period> todaysPeriods = allPeriods.stream()
                .sorted(Comparator.comparing(p -> Integer.parseInt(p.getName().replaceAll("[^\\d]", ""))))
                .limit(maxPeriodsToday)
                .collect(Collectors.toList());

        Map<Long, Integer> teacherPeriodCount = new HashMap<>();

        Set<String> optionalSubjects = Set.of("Yoga");
        Set<String> coreSubjects = Set.of("Mathematics", "Science", "SocialStudies", "Computer");
        Set<String> langSkillSubjects = Set.of("English", "Kannada", "GK");

        for (Classroom classroom : classrooms) {
            String className = classroom.getName();


            Map<String, Integer> groupCount = weeklySubjectGroupCountPerClass
                    .computeIfAbsent(className, k -> new HashMap<>());


            int optionalUsed = yogaAssignedPerClass.getOrDefault(className, 0);
            int coreUsed = groupCount.getOrDefault("core", 0);
            int langUsed = groupCount.getOrDefault("lang", 0);

            
            Map<String, Integer> subjectDailyUsage = new HashMap<>();

            for (Period period : todaysPeriods) {
                Subject selectedSubject = null;

                List<Subject> shuffledSubjects = new ArrayList<>(subjects);
                Collections.shuffle(shuffledSubjects);

                for (Subject subject : shuffledSubjects) {
                    String name = subject.getName();
                    int dailyUsed = subjectDailyUsage.getOrDefault(name, 0);

                    if (optionalSubjects.contains(name)) {
                        if (optionalUsed >= MAX_WEEKLY_YOGA || dailyUsed >= 1) continue;
                        selectedSubject = subject;
                        optionalUsed++;
                        yogaAssignedPerClass.put(className, optionalUsed);
                    } else if (coreSubjects.contains(name)) {
                        if (coreUsed >= MAX_WEEKLY_CORE || dailyUsed >= 2) continue;
                        selectedSubject = subject;
                        coreUsed++;
                        groupCount.put("core", coreUsed);
                    } else if (langSkillSubjects.contains(name)) {
                        if (langUsed >= MAX_WEEKLY_LANG_SKILLS || dailyUsed >= 1) continue;
                        selectedSubject = subject;
                        langUsed++;
                        groupCount.put("lang", langUsed);
                    }

                    if (selectedSubject != null) {
                        subjectDailyUsage.put(name, dailyUsed + 1);
                        break;
                    }
                }

                if (selectedSubject == null) {
                    logger.warn("❗ No suitable subject for {} on {} {}", className, date, period.getName());
                    continue;
                }

                final Subject finalSubject = selectedSubject;

                List<Teacher> availableTeachers = teachers.stream()
                        .filter(t -> t.getAvailablePeriods().contains(period.getName()))
                        .filter(t -> t.getSubjectsAndClasses().stream()
                                .anyMatch(pair -> pair.getSubject().equals(finalSubject.getName()) &&
                                        pair.getClasses().equals(className)))
                        .filter(t -> teacherPeriodCount.getOrDefault(t.getId(), 0) < maxTeacherPeriods)
                        .collect(Collectors.toList());

                if (availableTeachers.isEmpty()) {
                    logger.warn("❗ No teacher found for {} - {} - {}", className, finalSubject.getName(), period.getName());
                    continue;
                }

                Collections.shuffle(availableTeachers);
                Teacher assignedTeacher = availableTeachers.get(0);
                teacherPeriodCount.put(assignedTeacher.getId(), teacherPeriodCount.getOrDefault(assignedTeacher.getId(), 0) + 1);

                TimetableEntry entry = new TimetableEntry();
                entry.setDate(date);
                entry.setDay(day.toString());
                entry.setClassroom(classroom);
                entry.setPeriod(period);
                entry.setSubject(finalSubject);
                entry.setTeacher(assignedTeacher);

                timetableEntryRepository.save(entry);
            }
        }

        timetableEntryRepository.saveToFile();
        logger.info("✅ Timetable generated for {}", date);
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
        // Step 1: Clear existing entries
        timetableEntryRepository.clear();
        timetableEntryRepository.saveToFile();

        // Step 2: Clear counters
        weeklySubjectGroupCountPerClass.clear();
        yogaAssignedPerClass.clear();

        // Step 3: Generate each day in order
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
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
                        .filter(t -> t.getAvailablePeriods().contains(period))
                        .filter(t -> t.getSubjectsAndClasses().stream()
                                .anyMatch(pair -> pair.getSubject().equals(subject.getName()) &&
                                        pair.getClasses().equals(classroom)))
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
                        .filter(t -> t.getAvailablePeriods().contains(period))
                        .filter(t -> t.getSubjectsAndClasses().stream()
                                .anyMatch(pair -> pair.getSubject().equals(subject.getName()) &&
                                        pair.getClasses().equals(classroom)))
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
            logger.error(" ❌ Failed to export timetable to Excel", e);
        }
    }



    public Map<String, Map<String, Long>> getWeeklySubjectCountPerClass(LocalDate weekStartDate) {
        LocalDate monday = weekStartDate.with(DayOfWeek.MONDAY);
        LocalDate saturday = monday.plusDays(5);

        List<TimetableEntry> weeklyEntries = timetableEntryRepository.findAll().stream()
                .filter(entry -> !entry.getDate().isBefore(monday) && !entry.getDate().isAfter(saturday))
                .collect(Collectors.toList());

        // Outer map: className -> (subjectName -> count)
        Map<String, Map<String, Long>> subjectCountMap = new HashMap<>();

        for (TimetableEntry entry : weeklyEntries) {
            String className = entry.getClassroom().getName();
            String subjectName = entry.getSubject().getName();

            subjectCountMap
                    .computeIfAbsent(className, k -> new HashMap<>())
                    .merge(subjectName, 1L, Long::sum);
        }

        return subjectCountMap;
    }

}
