package com.example.school.controller;

import com.example.school.entity.Period;
import com.example.school.repository.json.PeriodJsonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/periods")
public class PeriodController {

    private final PeriodJsonRepository repo;

    public PeriodController(PeriodJsonRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Period> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Period> getById(@PathVariable Long id) {
        Period period = repo.findById(id);
        return period != null ? ResponseEntity.ok(period) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Period> create(@RequestBody Period period) {
        Period saved = repo.save(period);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Period> update(@PathVariable Long id, @RequestBody Period updated) {
        Period existing = repo.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setName(updated.getName());
        existing.setSession(updated.getSession());
        existing.setDuration(updated.getDuration());
        Period saved = repo.save(existing);
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