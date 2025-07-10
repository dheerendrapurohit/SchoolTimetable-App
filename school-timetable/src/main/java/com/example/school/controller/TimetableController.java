package com.example.school.controller;

import com.example.school.entity.TimetableEntry;
import com.example.school.repository.json.TimetableEntryJsonRepository;
import com.example.school.service.TimetableService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    private final TimetableEntryJsonRepository repo;
    private final TimetableService service;

    public TimetableController(TimetableEntryJsonRepository repo, TimetableService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping
    public List<TimetableEntry> getAll() {
        return repo.findAll();
    }

    @PostMapping("/generate")
    public String generateWeek() {
        service.generateTimetableForWeek();
        return "Timetable generated for the week.";
    }

    @GetMapping("/class/{className}")
    public List<TimetableEntry> getByClass(@PathVariable String className) {
        System.out.println("Fetching timetable for class: " + className);
        return repo.findAll().stream()
                .filter(e -> e.getClassroom() != null && className.equalsIgnoreCase(e.getClassroom().getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/teacher/{teacherName}")
    public List<TimetableEntry> getByTeacher(@PathVariable String teacherName) {
        return repo.findAll().stream()
                .filter(e -> e.getTeacher() != null && teacherName.equalsIgnoreCase(e.getTeacher().getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/day/{day}")
    public List<TimetableEntry> getByDay(@PathVariable String day) {
        return repo.findAll().stream()
                .filter(e -> day.equalsIgnoreCase(e.getDay()))
                .collect(Collectors.toList());
    }
}
