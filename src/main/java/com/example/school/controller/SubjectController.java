package com.example.school.controller;

import com.example.school.entity.Subject;
import com.example.school.repository.json.SubjectJsonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectJsonRepository repo;

    public SubjectController(SubjectJsonRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Subject> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getById(@PathVariable Long id) {
        Subject subject = repo.findById(id);
        return subject != null ? ResponseEntity.ok(subject) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Subject> create(@RequestBody Subject subject) {
        Subject saved = repo.save(subject);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subject> update(@PathVariable Long id, @RequestBody Subject updated) {
        Subject existing = repo.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setName(updated.getName());
        Subject saved = repo.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
