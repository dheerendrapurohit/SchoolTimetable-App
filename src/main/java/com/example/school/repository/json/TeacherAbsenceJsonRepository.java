package com.example.school.repository.json;

import com.example.school.entity.TeacherAbsence;
import org.springframework.stereotype.Repository;

@Repository
public class TeacherAbsenceJsonRepository extends JsonRepository<TeacherAbsence> {
    public TeacherAbsenceJsonRepository() {
        super("data/teacher_absences.json", TeacherAbsence.class);
    }
}
