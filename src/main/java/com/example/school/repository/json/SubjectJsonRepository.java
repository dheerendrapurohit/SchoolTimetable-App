package com.example.school.repository.json;

import com.example.school.entity.Subject;
import org.springframework.stereotype.Repository;

@Repository
public class SubjectJsonRepository extends JsonRepository<Subject> {
    public SubjectJsonRepository() {
        super("subjects.json", Subject.class);
    }
}
