package com.example.school.repository.json;

import com.example.school.entity.Teacher;
import org.springframework.stereotype.Repository;

@Repository
public class TeacherJsonRepository extends JsonRepository<Teacher> {
    public TeacherJsonRepository() {
        super("teachers.json", Teacher.class);
    }
}
