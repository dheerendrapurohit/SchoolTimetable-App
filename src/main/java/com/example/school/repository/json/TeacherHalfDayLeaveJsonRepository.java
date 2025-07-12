package com.example.school.repository.json;

import com.example.school.entity.TeacherHalfDayLeave;
import org.springframework.stereotype.Repository;

@Repository
public class TeacherHalfDayLeaveJsonRepository extends JsonRepository<TeacherHalfDayLeave> {
    public TeacherHalfDayLeaveJsonRepository() {
        super("teacher_halfday_leaves.json", TeacherHalfDayLeave.class);
    }
}
