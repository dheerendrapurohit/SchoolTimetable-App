package com.example.school.controller;

import com.example.school.entity.TimetableEntry;
import com.example.school.repository.json.TimetableEntryJsonRepository;
import com.example.school.service.TimetableService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    private final TimetableEntryJsonRepository repo;
    private final TimetableService timetableService;

    public TimetableController(TimetableEntryJsonRepository repo, TimetableService timetableService) {
        this.repo = repo;
        this.timetableService = timetableService;
    }

    //  Return only entries for the current week (Monday to Sunday)
    @GetMapping
    public List<TimetableEntry> getThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        return repo.findAll().stream()
                .filter(e -> e.getDate() != null &&
                        !e.getDate().isBefore(monday) &&
                        !e.getDate().isAfter(sunday))
                .collect(Collectors.toList());
    }

    //  Generate timetable for this week
    @PostMapping("/generate")
    public String generateWeek() {
        timetableService.generateTimetableForWeek();
        return "Timetable generated for the week.";
    }




    //  Filter by class name
    @GetMapping("/class/{className}")
    public List<TimetableEntry> getByClass(@PathVariable String className) {
        return repo.findAll().stream()
                .filter(e -> e.getClassroom() != null &&
                        className.equalsIgnoreCase(e.getClassroom().getName()))
                .collect(Collectors.toList());
    }

    //  Filter by teacher name
    @GetMapping("/teacher/{teacherName}")
    public List<TimetableEntry> getByTeacher(@PathVariable String teacherName) {
        return repo.findAll().stream()
                .filter(e -> e.getTeacher() != null &&
                        teacherName.equalsIgnoreCase(e.getTeacher().getName()))
                .collect(Collectors.toList());
    }

    //  Filter by exact date (YYYY-MM-DD)
    @GetMapping("/date/{date}")
    public List<TimetableEntry> getByDate(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        return repo.findAll().stream()
                .filter(e -> e.getDate() != null &&
                        e.getDate().equals(localDate))
                .collect(Collectors.toList());
    }

    @GetMapping("/day/{day}")
    public List<TimetableEntry> getByDay(@PathVariable String day) {
        return repo.findAll().stream()
                .filter(e -> e.getDay() != null &&
                        e.getDay().equalsIgnoreCase(day))
                .collect(Collectors.toList());
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/download-latest-excel")
    public ResponseEntity<Resource> downloadLatestExcel() throws IOException {
        File exportDir = new File("exports");
        if (!exportDir.exists() || !exportDir.isDirectory()) {
            return ResponseEntity.notFound().build();
        }

        // Find the latest file
        File[] files = exportDir.listFiles((dir, name) -> name.endsWith(".xlsx"));
        if (files == null || files.length == 0) {
            return ResponseEntity.notFound().build();
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        File latestFile = files[0];

        InputStreamResource resource = new InputStreamResource(new FileInputStream(latestFile));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + latestFile.getName())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }





}
