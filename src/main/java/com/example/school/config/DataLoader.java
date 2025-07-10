package com.example.school.config;

import com.example.school.entity.*;
import com.example.school.repository.json.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final TeacherJsonRepository teacherRepository;
    private final SubjectJsonRepository subjectRepository;
    private final ClassroomJsonRepository classroomRepository;
    private final PeriodJsonRepository periodRepository;
    private final TimetableEntryJsonRepository timetableEntryRepository;

    public DataLoader(TeacherJsonRepository teacherRepository,
                      SubjectJsonRepository subjectRepository,
                      ClassroomJsonRepository classroomRepository,
                      PeriodJsonRepository periodRepository,
                      TimetableEntryJsonRepository timetableEntryRepository) {
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.classroomRepository = classroomRepository;
        this.periodRepository = periodRepository;
        this.timetableEntryRepository = timetableEntryRepository;
    }

    @PostConstruct
    public void init() {
        logger.info("Starting init()");
        try {
            loadData();
            timetableEntryRepository.saveToFile();
        } catch (Exception e) {
            logger.error("Error during init()", e);
        }
        logger.info("init() complete");
    }

    @Transactional
    public void loadData() {
        subjectRepository.clear();
        teacherRepository.clear();
        classroomRepository.clear();
        periodRepository.clear();
        timetableEntryRepository.clear();
        logger.debug("All repositories cleared.");

        logger.debug("Loading data into repositories...");
        loadSubjects();
        loadClassrooms();
        loadPeriods();
        loadTeachers();
        loadSampleTimetableEntries();
        logger.info("Data load complete");
    }

    private void loadSubjects() {
        List<String> subjects = List.of("English", "Mathematics", "Kannada", "Science", "SocialStudies", "Yoga");
        subjects.forEach(name -> {
            Subject s = new Subject();
            s.setName(name);
            subjectRepository.save(s);
        });
        logger.debug("Subjects loaded: {}", subjects.size());
    }

    private void loadClassrooms() {
        List<String> rooms = List.of("PKG", "LKG", "UKG", "1", "2", "3", "4", "5");
        rooms.forEach(name -> {
            Classroom c = new Classroom();
            c.setName(name);
            classroomRepository.save(c);
        });
        logger.debug("Classrooms loaded: {}", rooms.size());
    }

    private void loadPeriods() {
        for (int i = 1; i <= 4; i++) {
            Period p = new Period();
            p.setName("P" + i);
            p.setDuration(45);
            p.setSession("Morning");
            periodRepository.save(p);
        }

        for (int i = 5; i <= 7; i++) {
            Period p = new Period();
            p.setName("P" + i);
            p.setDuration(40);
            p.setSession("Afternoon");
            periodRepository.save(p);
        }
        logger.debug("Periods loaded");
    }

    private void loadTeachers() {
        String[][] teacherSubjects = {
                {"English"}, {"Mathematics"}, {"Science"}, {"Mathematics", "Science"},
                {"Kannada"}, {"Kannada", "SocialStudies"}, {"SocialStudies"}, {"Yoga"},
                {"English"}, {"Mathematics"}, {"Science"}, {"Mathematics", "Science"},
                {"Kannada"}, {"Kannada", "SocialStudies"}, {"SocialStudies"}, {"English"},
                {"Mathematics"}, {"Science"}, {"Mathematics", "Science"}, {"Kannada"},
                {"Kannada", "SocialStudies"}, {"SocialStudies"}, {"English"}, {"Mathematics"},
                {"Science"}, {"Mathematics", "Science"}, {"Kannada"}, {"Kannada", "SocialStudies"},
                {"SocialStudies"}, {"Mathematics"}
        };

        IntStream.range(0, teacherSubjects.length).forEach(i -> {
            Teacher t = new Teacher();
            t.setName("T" + (i + 1));

            List<String> periods = (i % 2 == 0)
                    ? List.of("P1", "P2", "P3", "P4")
                    : List.of("P1","P4", "P5", "P6", "P7");
            t.setAvailablePeriods(periods);
            t.setSubjects(Arrays.asList(teacherSubjects[i]));
            teacherRepository.save(t);
        });
        logger.debug("Teachers loaded: 30");
    }

    private void loadSampleTimetableEntries() {
        timetableEntryRepository.clear();
        logger.debug("Old timetable data cleared.");

        List<Teacher> teachers = teacherRepository.findAll();
        List<Subject> subjects = subjectRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();
        List<Period> periods = periodRepository.findAll();

        Map<String, Boolean> yogaAssigned = new HashMap<>(); // track Yoga per class
        Random random = new Random();
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);

        for (Classroom classroom : classrooms) {
            yogaAssigned.put(classroom.getName(), false);

            for (int i = 0; i < 6; i++) {
                LocalDate date = monday.plusDays(i);
                DayOfWeek day = date.getDayOfWeek();
                boolean isSaturday = day == DayOfWeek.SATURDAY;
                List<Period> todaysPeriods = isSaturday
                        ? periods.stream().filter(p -> p.getName().matches("P[1-4]")).toList()
                        : periods;

                Set<String> uniqueSubjectsUsed = new HashSet<>();
                Map<String, Integer> subjectUsageCount = new HashMap<>();

                for (Period period : todaysPeriods) {
                    Subject subject = null;

                    // Try assigning Yoga if not already assigned for this class
                    if (!yogaAssigned.get(classroom.getName())
                            && !uniqueSubjectsUsed.contains("Yoga")
                            && subjectUsageCount.getOrDefault("Yoga", 0) == 0
                            && uniqueSubjectsUsed.size() < 3) {

                        subject = subjects.stream()
                                .filter(s -> s.getName().equalsIgnoreCase("Yoga"))
                                .findFirst().orElse(null);

                        if (subject != null) {
                            yogaAssigned.put(classroom.getName(), true);
                            uniqueSubjectsUsed.add("Yoga");
                            subjectUsageCount.put("Yoga", 1);
                        }
                    }

                    // If Yoga not assigned or skipped, pick a subject under limits
                    if (subject == null) {
                        List<Subject> availableSubjects = subjects.stream()
                                .filter(s -> {
                                    int count = subjectUsageCount.getOrDefault(s.getName(), 0);
                                    return count < 3;
                                })
                                .filter(s -> {
                                    if (uniqueSubjectsUsed.size() < 3) return true;
                                    return uniqueSubjectsUsed.contains(s.getName());
                                })
                                .filter(s -> !s.getName().equalsIgnoreCase("Yoga")
                                        || (!yogaAssigned.get(classroom.getName())
                                        && !uniqueSubjectsUsed.contains("Yoga")
                                        && subjectUsageCount.getOrDefault("Yoga", 0) == 0))
                                .toList();

                        if (availableSubjects.isEmpty()) continue;

                        subject = availableSubjects.get(random.nextInt(availableSubjects.size()));
                        subjectUsageCount.put(subject.getName(), subjectUsageCount.getOrDefault(subject.getName(), 0) + 1);
                        uniqueSubjectsUsed.add(subject.getName());
                    }

                    // Get applicable teachers
                    Subject finalSubject = subject;
                    List<Teacher> applicableTeachers = teachers.stream()
                            .filter(t -> t.getSubjects().contains(finalSubject.getName()))
                            .filter(t -> t.getAvailablePeriods().contains(period.getName()))
                            .toList();

                    if (applicableTeachers.isEmpty()) continue;

                    Teacher teacher = applicableTeachers.get(random.nextInt(applicableTeachers.size()));

                    TimetableEntry entry = new TimetableEntry();
                    entry.setDate(date);
                    entry.setDay(day.toString());
                    entry.setClassroom(classroom);
                    entry.setPeriod(period);
                    entry.setSubject(subject);
                    entry.setTeacher(teacher);

                    timetableEntryRepository.save(entry);
                }
            }
        }

        logger.info("Strict, fully-populated sample timetable loaded.");
    }






    private boolean isTeacherAvailableForPeriod(Teacher teacher, Period period) {
        return teacher.getAvailablePeriods().contains(period.getName());
    }
}
