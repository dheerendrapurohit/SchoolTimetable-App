package com.example.school.controller;

import com.example.school.entity.TeacherAbsence;
import com.example.school.entity.TeacherHalfDayLeave;
import com.example.school.repository.json.TeacherAbsenceJsonRepository;
import com.example.school.repository.json.TeacherHalfDayLeaveJsonRepository;
import com.example.school.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private TeacherHalfDayLeaveJsonRepository halfDayRepo;

    @Autowired
    private TimetableService timetableService;

    // ✅ Full-day absence
    @PostMapping("/mark")
    public String markTeacherAbsent(@RequestBody TeacherAbsence absence) {
        absence.setDay(absence.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        absenceRepo.save(absence);

        timetableService.handleTeacherAbsence(absence.getName(), absence.getDate());

        return "✅ Marked " + absence.getName() + " absent for full day: " + absence.getDate() + ". Reassigned classes.";
    }

    // ✅ Half-day absence
    @PostMapping("/halfday")
    public String markHalfDayAbsent(@RequestBody TeacherHalfDayLeave leave) {
        List<String> periods = leave.getPeriods();
        if (periods == null || periods.isEmpty()) {
            return "❌ Please provide at least one period (e.g., P1, P2, P3)";
        }

        halfDayRepo.save(leave);
        timetableService.handleTeacherAbsenceForPeriods(leave.getName(), leave.getDate(), periods);

        return "✅ Marked " + leave.getName() + " absent for periods " + periods + " on " + leave.getDate() + ". Reassigned classes.";
    }

    // ✅ All full-day absences
    @GetMapping
    public List<TeacherAbsence> getAllAbsences() {
        return absenceRepo.findAll();
    }

    // ✅ All half-day absences
    @GetMapping("/halfday")
    public List<TeacherHalfDayLeave> getAllHalfDayLeaves() {
        return halfDayRepo.findAll();
    }
}
