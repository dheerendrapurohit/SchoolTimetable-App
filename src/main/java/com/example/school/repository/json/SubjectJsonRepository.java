package com.example.school.repository.json;

import com.example.school.entity.Subject;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubjectJsonRepository extends JsonRepository<Subject> {

    public SubjectJsonRepository() {
        super("subjects.json", Subject.class);
    }

    public Subject findByName(String name) {
        return findAll().stream()
                .filter(subject -> subject.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
