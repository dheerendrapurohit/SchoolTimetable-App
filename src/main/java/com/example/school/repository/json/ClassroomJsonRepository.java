package com.example.school.repository.json;

import com.example.school.entity.Classroom;
import org.springframework.stereotype.Repository;

@Repository
public class ClassroomJsonRepository extends JsonRepository<Classroom> {
    public ClassroomJsonRepository() {
        super("classrooms.json", Classroom.class);
    }
}
