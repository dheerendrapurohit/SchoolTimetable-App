package com.example.school.controller;

import com.example.school.entity.TeacherAbsence;
import com.example.school.repository.json.TeacherAbsenceJsonRepository;
import com.example.school.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/absences")
public class TeacherAbsenceController {

    @Autowired
    private TeacherAbsenceJsonRepository absenceRepo;

    @Autowired
    private TimetableService timetableService;

    @PostMapping("/mark")
    public String markTeacherAbsent(@RequestBody TeacherAbsence absence) {
        // Ensure day is derived from date
        absence.setDay(absence.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        absenceRepo.save(absence);
        timetableService.handleTeacherAbsence(absence.getName(), absence.getDate());

        return "Marked " + absence.getName() + " absent for " + absence.getDate() + " and reassigned classes.";
    }


    @GetMapping
    public List<TeacherAbsence> getAllAbsences() {
        return absenceRepo.findAll();
    }
}
