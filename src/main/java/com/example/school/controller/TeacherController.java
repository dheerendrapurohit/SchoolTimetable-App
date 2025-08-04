package com.example.school.controller;

import com.example.school.entity.Teacher;
import com.example.school.service.TimetableService;
import com.example.school.repository.json.TeacherJsonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherJsonRepository repo;
    private final TimetableService timetableService;

    public TeacherController(TeacherJsonRepository repo, TimetableService timetableService) {
        this.repo = repo;
        this.timetableService = timetableService;
    }

    @GetMapping
    public List<Teacher> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getById(@PathVariable Long id) {
        Teacher teacher = repo.findById(id);
        return teacher != null ? ResponseEntity.ok(teacher) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Teacher> create(@RequestBody Teacher teacher) {
        // ✅ Uses new structure with subjectsAndClasses
        Teacher saved = repo.save(teacher);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Teacher> update(@PathVariable Long id, @RequestBody Teacher updated) {
        Teacher existing = repo.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        existing.setName(updated.getName());
        existing.setAvailablePeriods(updated.getAvailablePeriods());
        existing.setSubjectsAndClasses(updated.getSubjectsAndClasses()); // ✅ Updated field

        Teacher saved = repo.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }


}
