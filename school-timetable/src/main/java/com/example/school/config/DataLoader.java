package com.example.school.config;

import com.example.school.entity.*;
import com.example.school.repository.json.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
            logger.error(" Error during init()", e);
        }
        logger.info(" init() complete");
    }

    @Transactional
    public void loadData() {
        logger.debug(" Loading data into repositories...");
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
        logger.debug(" Subjects loaded: {}", subjects.size());
    }

    private void loadClassrooms() {
        List<String> rooms = List.of("PKG-S", "LKG-S", "LKG-B", "UKG-S", "UKG-B", "1-S", "1-B", "2-S", "2-B", "3-S", "3-B", "4-S", "4-B", "5-S", "5-B");
        rooms.forEach(name -> {
            Classroom c = new Classroom();
            c.setName(name);
            classroomRepository.save(c);
        });
        logger.debug(" Classrooms loaded: {}", rooms.size());
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
        logger.debug(" Periods loaded");
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

        IntStream.range(0, 30).forEach(i -> {
            Teacher t = new Teacher();
            t.setName("T" + (i + 1));

            List<String> periods = (i % 2 == 0)
                    ? List.of("P4", "P5", "P6", "P7")
                    : List.of("P1", "P2", "P3", "P4");
            t.setAvailablePeriods(periods);
            t.setSubjects(Arrays.asList(teacherSubjects[i]));
            teacherRepository.save(t);
        });
        logger.debug(" Teachers loaded: 30");
    }

    private void loadSampleTimetableEntries() {
        timetableEntryRepository.clear();

        List<Teacher> teachers = teacherRepository.findAll();
        List<Subject> subjects = subjectRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();
        List<Period> periods = periodRepository.findAll();

        String[] fullDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] halfDay = {"Saturday"};
        Map<String, Integer> yogaCount = new HashMap<>();

        Random random = new Random();

        for (Classroom classroom : classrooms) {
            for (String day : fullDays) {
                for (Period period : periods) {
                    TimetableEntry entry = new TimetableEntry();
                    entry.setDay(day);
                    entry.setClassroom(classroom);
                    entry.setPeriod(period);

                    Subject subject;

                    if (!yogaCount.containsKey(classroom.getName())) {
                        subject = subjects.stream()
                                .filter(s -> s.getName().equalsIgnoreCase("Yoga"))
                                .findFirst()
                                .orElse(null);
                        if (subject == null) continue;
                        yogaCount.put(classroom.getName(), 1);
                    } else {
                        List<Subject> nonYogaSubjects = subjects.stream()
                                .filter(s -> !s.getName().equalsIgnoreCase("Yoga"))
                                .toList();

                        if (nonYogaSubjects.isEmpty()) continue;

                        subject = nonYogaSubjects.get(random.nextInt(nonYogaSubjects.size()));
                    }

                    entry.setSubject(subject);

                    List<Teacher> applicableTeachers = teachers.stream()
                            .filter(t -> t.getSubjects().contains(subject.getName()))
                            .filter(t -> isTeacherAvailableForPeriod(t, period))
                            .toList();

                    if (!applicableTeachers.isEmpty()) {
                        entry.setTeacher(applicableTeachers.get(random.nextInt(applicableTeachers.size())));
                        timetableEntryRepository.save(entry);
                    }
                }
            }

            for (String day : halfDay) {
                for (Period period : periods.stream().filter(p -> p.getName().matches("p[1-4]")).toList()) {
                    TimetableEntry entry = new TimetableEntry();
                    entry.setDay(day);
                    entry.setClassroom(classroom);
                    entry.setPeriod(period);

                    List<Subject> nonYogaSubjects = subjects.stream()
                            .filter(s -> !s.getName().equalsIgnoreCase("Yoga"))
                            .toList();

                    if (nonYogaSubjects.isEmpty()) continue;

                    Subject subject = nonYogaSubjects.get(random.nextInt(nonYogaSubjects.size()));
                    entry.setSubject(subject);

                    List<Teacher> applicableTeachers = teachers.stream()
                            .filter(t -> t.getSubjects().contains(subject.getName()))
                            .filter(t -> isTeacherAvailableForPeriod(t, period))
                            .toList();

                    if (!applicableTeachers.isEmpty()) {
                        entry.setTeacher(applicableTeachers.get(random.nextInt(applicableTeachers.size())));
                        timetableEntryRepository.save(entry);
                    }
                }
            }
        }

        timetableEntryRepository.saveToFile();
        logger.info(" Sample timetable entries loaded.");
    }

    private boolean isTeacherAvailableForPeriod(Teacher teacher, Period period) {
        int periodNumber = Integer.parseInt(period.getName().substring(1));
        boolean isOddTeacher = Integer.parseInt(teacher.getName().substring(1)) % 2 != 0;

        return isOddTeacher
                ? (periodNumber >= 1 && periodNumber <= 4)
                : (periodNumber >= 4 && periodNumber <= 7);
    }
}
