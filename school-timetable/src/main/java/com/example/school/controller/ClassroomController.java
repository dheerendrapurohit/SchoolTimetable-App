package com.example.school.controller;

import com.example.school.entity.Classroom;
import com.example.school.repository.json.ClassroomJsonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomJsonRepository repo;

    public ClassroomController(ClassroomJsonRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Classroom> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Classroom> getById(@PathVariable Long id) {
        Classroom classroom = repo.findById(id);
        return (classroom != null) ? ResponseEntity.ok(classroom) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Classroom create(@RequestBody Classroom classroom) {
        return repo.save(classroom);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Classroom> update(@PathVariable Long id, @RequestBody Classroom updated) {
        Classroom existing = repo.findById(id);
        if (existing == null) return ResponseEntity.notFound().build();

        existing.setName(updated.getName());
        return ResponseEntity.ok(repo.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
