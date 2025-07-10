package com.example.school.controller;

import com.example.school.entity.TimetableEntry;
import com.example.school.repository.json.TimetableEntryJsonRepository;
import com.example.school.service.TimetableService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
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

    //  Return entries for the current week (Monday to Sunday)
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

    //  Generate timetable for the current week
    @PostMapping("/generate")
    public String generateWeek() {
        timetableService.generateTimetableForWeek();
        return "Timetable generated for the week.";
    }

    //  Return current week's timetable filtered by class name
    @GetMapping("/week/class/{className}")
    public List<TimetableEntry> getThisWeekByClass(@PathVariable String className) {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate saturday = monday.plusDays(5); // Exclude Sunday

        return repo.findAll().stream()
                .filter(e -> e.getDate() != null &&
                        !e.getDate().isBefore(monday) &&
                        !e.getDate().isAfter(saturday) &&
                        e.getClassroom() != null &&
                        className.equalsIgnoreCase(e.getClassroom().getName()))
                .collect(Collectors.toList());
    }


    //  NEW: Generate timetable between two dates (excluding Sundays)
    @PostMapping("/generate-between")
    public ResponseEntity<String> generateBetweenDates(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body("Start date must be before or equal to end date.");
        }

        timetableService.generateTimetableBetweenDates(startDate, endDate);
        return ResponseEntity.ok("Timetable generated from " + startDate + " to " + endDate + ".");
    }

    @PostMapping("/generate-day")
    public ResponseEntity<String> generateTimetableForDay(@RequestParam String date) {
        System.out.println("🔍 Received request to generate timetable for: " + date);
        try {
            LocalDate targetDate = LocalDate.parse(date);
            timetableService.generateTimetableForSingleDay(targetDate);
            return ResponseEntity.ok(" Timetable generated for " + targetDate);
        } catch (Exception e) {
            e.printStackTrace(); // Log the full error in the console
            return ResponseEntity.status(500).body(" Error generating timetable: " + e.getMessage());
        }
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
                .filter(e -> e.getDate() != null && e.getDate().equals(localDate))
                .collect(Collectors.toList());
    }

    //  Filter by day name
    @GetMapping("/day/{day}")
    public List<TimetableEntry> getByDay(@PathVariable String day) {
        return repo.findAll().stream()
                .filter(e -> e.getDay() != null &&
                        e.getDay().equalsIgnoreCase(day))
                .collect(Collectors.toList());
    }

    //  Download latest exported Excel timetable
    @GetMapping("/download-latest-excel")
    public ResponseEntity<Resource> downloadLatestExcel() throws IOException {
        File exportDir = new File("exports");
        if (!exportDir.exists() || !exportDir.isDirectory()) {
            return ResponseEntity.notFound().build();
        }

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
