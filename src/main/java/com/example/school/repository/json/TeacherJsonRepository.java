package com.example.school.repository.json;

import com.example.school.entity.Teacher;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

@Repository
public class TeacherJsonRepository extends JsonRepository<Teacher> {

    public TeacherJsonRepository() {
        super("teachers.json", Teacher.class);
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ Loading teachers from teachers.json");
        System.out.println("✅ Total teachers loaded: " + findAll().size());
        // Optional: print teacher names
        findAll().forEach(t -> System.out.println("   - " + t.getName()));
    }
}
