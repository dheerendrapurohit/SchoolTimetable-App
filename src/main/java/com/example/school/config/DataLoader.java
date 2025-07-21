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
        List<String> subjects = List.of("English", "Mathematics", "Kannada", "Science", "SocialStudies", "Yoga" ,"Computer", "GK");
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
                // English (5 teachers)
                { "English" }, { "English" }, { "English", "SocialStudies" }, { "English", "GK" }, { "English", "Kannada" },

                // Mathematics (5 teachers)
                { "Mathematics" }, { "Mathematics" }, { "Mathematics", "Science" }, { "Mathematics", "Computer" }, { "Mathematics", "GK" },

                // Science (4 teachers)
                { "Science" }, { "Science", "Mathematics" }, { "Science", "GK" }, { "Science", "Computer" },

                // Kannada (4 teachers)
                { "Kannada" }, { "Kannada", "SocialStudies" }, { "Kannada", "GK" }, { "Kannada", "English" },

                // Social Studies (4 teachers)
                { "SocialStudies" }, { "SocialStudies", "GK" }, { "SocialStudies", "Kannada" }, { "SocialStudies", "English" },

                // Computer (3 teachers)
                { "Computer" }, { "Computer", "Mathematics" }, { "Computer", "GK" },

                // GK (3 teachers)
                { "GK" }, { "GK", "English" }, { "GK", "Science" },

                // Yoga (1 teacher only)
                { "Yoga" }
        };


        List<String> lowerClasses = List.of("PKG", "LKG", "UKG", "1");
        List<String> higherClasses = List.of("2", "3", "4", "5");

        Map<String, List<Integer>> subjectToTeacherIndices = new HashMap<>();
        for (int i = 0; i < teacherSubjects.length; i++) {
            for (String subject : teacherSubjects[i]) {
                subjectToTeacherIndices.computeIfAbsent(subject, s -> new ArrayList<>()).add(i);
            }
        }

        Map<Integer, Set<String>> teacherAvailableClassesMap = new HashMap<>();

        for (Map.Entry<String, List<Integer>> entry : subjectToTeacherIndices.entrySet()) {
            List<Integer> indices = entry.getValue();
            int mid = (int) Math.ceil(indices.size() / 2.0);

            for (int j = 0; j < indices.size(); j++) {
                int teacherIndex = indices.get(j);
                List<String> assigned = (j < mid) ? lowerClasses : higherClasses;

                teacherAvailableClassesMap
                        .computeIfAbsent(teacherIndex, k -> new HashSet<>())
                        .addAll(assigned);
            }
        }

        IntStream.range(0, teacherSubjects.length).forEach(i -> {
            Teacher t = new Teacher();
            t.setName("T" + (i + 1));

            List<String> periods = (i % 2 == 0)
                    ? List.of("P1", "P2", "P3", "P4")
                    : List.of("P1", "P4", "P5", "P6", "P7");
            t.setAvailablePeriods(periods);

            List<String> subjects = Arrays.asList(teacherSubjects[i]);
            t.setSubjects(subjects);

            if (subjects.contains("Yoga")) {
                t.setAvailableClasses(List.of("PKG", "LKG", "UKG", "1", "2", "3", "4", "5"));
            } else {
                List<String> availableClasses = new ArrayList<>(teacherAvailableClassesMap.getOrDefault(i, new HashSet<>()));
                t.setAvailableClasses(availableClasses);
            }

            teacherRepository.save(t);
        });


        logger.debug("Teachers loaded: {}", teacherSubjects.length);

    }


    private void loadSampleTimetableEntries() {
        timetableEntryRepository.clear();
        logger.debug("Old timetable data cleared.");

        List<Teacher> teachers = teacherRepository.findAll();
        List<Subject> subjects = subjectRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();
        List<Period> periods = periodRepository.findAll();

        Random random = new Random();
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);

        Subject yogaSubject = subjects.stream()
                .filter(s -> s.getName().equalsIgnoreCase("Yoga"))
                .findFirst()
                .orElse(null);

        Map<String, LocalDate> yogaDatePerClass = new HashMap<>();
        Map<String, String> yogaPeriodPerClass = new HashMap<>();
        Set<String> yogaSlotUsed = new HashSet<>();

        for (Classroom classroom : classrooms) {
            boolean yogaAssigned = false;

            outer:
            for (int i = 0; i < 6; i++) {
                LocalDate date = monday.plusDays(i);
                DayOfWeek day = date.getDayOfWeek();
                boolean isSaturday = day == DayOfWeek.SATURDAY;

                List<Period> todaysPeriods = isSaturday
                        ? periods.stream().filter(p -> !p.getName().equals("P1")).limit(4).toList()
                        : periods.stream().filter(p -> !p.getName().equals("P1")).toList();

                List<String> shuffledPeriods = todaysPeriods.stream().map(Period::getName).collect(Collectors.toList());
                Collections.shuffle(shuffledPeriods);

                for (String periodName : shuffledPeriods) {
                    String yogaSlotKey = date + "_" + periodName;

                    boolean teacherAvailable = teachers.stream()
                            .anyMatch(t ->
                                    t.getSubjects().contains("Yoga")
                                            && t.getAvailablePeriods().contains(periodName)
                                            && t.getAvailableClasses().contains(classroom.getName())
                            );

                    if (teacherAvailable && !yogaSlotUsed.contains(yogaSlotKey)) {
                        yogaDatePerClass.put(classroom.getName(), date);
                        yogaPeriodPerClass.put(classroom.getName(), periodName);
                        yogaSlotUsed.add(yogaSlotKey);
                        yogaAssigned = true;
                        break outer;
                    }
                }
            }

            if (!yogaAssigned) {
                logger.warn("Could not assign Yoga to class {} due to teacher or slot unavailability", classroom.getName());
            }
        }

        for (Classroom classroom : classrooms) {
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


                    if (date.equals(yogaDatePerClass.get(classroom.getName())) &&
                            period.getName().equals(yogaPeriodPerClass.get(classroom.getName())) &&
                            yogaSubject != null) {

                        subject = yogaSubject;
                        uniqueSubjectsUsed.add("Yoga");
                        subjectUsageCount.put("Yoga", 1);
                    }


                    if (subject == null) {
                        List<Subject> availableSubjects = subjects.stream()
                                .filter(s -> subjectUsageCount.getOrDefault(s.getName(), 0) < 2)
                                .filter(s -> uniqueSubjectsUsed.size() < 6 || uniqueSubjectsUsed.contains(s.getName()))
                                .filter(s -> !s.getName().equalsIgnoreCase("Yoga"))
                                .toList();

                        if (availableSubjects.isEmpty()) {
                            logger.warn("No available subjects for {} {} {}", classroom.getName(), date, period.getName());
                            continue;
                        }

                        subject = availableSubjects.get(random.nextInt(availableSubjects.size()));
                        subjectUsageCount.put(subject.getName(), subjectUsageCount.getOrDefault(subject.getName(), 0) + 1);
                        uniqueSubjectsUsed.add(subject.getName());
                    }

                    // Assign teacher for subject
                    Subject finalSubject = subject;
                    List<Teacher> applicableTeachers = teachers.stream()
                            .filter(t -> t.getSubjects().contains(finalSubject.getName()))
                            .filter(t -> t.getAvailablePeriods().contains(period.getName()))
                            .filter(t -> t.getAvailableClasses().contains(classroom.getName()))
                            .toList();

                    if (applicableTeachers.isEmpty()) {
                        applicableTeachers = teachers.stream()
                                .filter(t -> t.getSubjects().contains(finalSubject.getName()))
                                .filter(t -> t.getAvailablePeriods().contains(period.getName()))
                                .toList();

                        if (!applicableTeachers.isEmpty()) {
                            logger.warn("Used fallback teacher for {} {} {} {}", classroom.getName(), date, period.getName(), subject.getName());
                        } else {
                            logger.warn("No teacher found for {} {} {} {}", classroom.getName(), date, period.getName(), subject.getName());
                            continue;
                        }
                    }

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

        logger.info("Yoga-aware, conflict-free timetable loaded.");
    }
}