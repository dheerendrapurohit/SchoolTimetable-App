package com.example.school.service;

import com.example.school.entity.*;
import com.example.school.repository.json.*;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimetableService {

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
        List<Classroom> classrooms = classroomRepository.findAll();
        List<Subject> subjects = subjectRepository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();
        List<Period> periods = periodRepository.findAll();

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        for (Classroom classroom : classrooms) {
            Map<String, Integer> subjectCount = new HashMap<>();

            for (String day : days) {
                int periodLimit = day.equals("Saturday") ? 4 : 7;

                for (int i = 1; i <= periodLimit; i++) {
                    String periodName = "P" + i;
                    Period period = periods.stream()
                            .filter(p -> p.getName().equals(periodName))
                            .findFirst()
                            .orElse(null);

                    if (period == null) continue;

                    Subject subjectToTeach = findBalancedSubject(subjects, subjectCount);
                    if (subjectToTeach == null) continue;

                    Teacher assignedTeacher = findAvailableTeacher(teachers, subjectToTeach.getName(), periodName);
                    if (assignedTeacher == null) continue;

                    TimetableEntry entry = new TimetableEntry();
                    entry.setClassroom(classroom);
                    entry.setPeriod(period);
                    entry.setDay(day);
                    entry.setSubject(subjectToTeach);
                    entry.setTeacher(assignedTeacher);

                    repository.save(entry);

                    subjectCount.put(subjectToTeach.getName(),
                            subjectCount.getOrDefault(subjectToTeach.getName(), 0) + 1);
                }
            }
        }
    }

    private Subject findBalancedSubject(List<Subject> subjects, Map<String, Integer> subjectCount) {
        for (Subject subject : subjects) {
            int count = subjectCount.getOrDefault(subject.getName(), 0);
            if (subject.getName().equalsIgnoreCase("Yoga") && count >= 1) continue;
            if (count < 5 || subject.getName().equalsIgnoreCase("Yoga"))
                return subject;
        }
        return null;
    }

    private Teacher findAvailableTeacher(List<Teacher> teachers, String subject, String period) {
        for (Teacher t : teachers) {
            if (t.getSubjects().contains(subject) && t.getAvailablePeriods().contains(period)) {
                return t;
            }
        }
        return null;
    }
}
