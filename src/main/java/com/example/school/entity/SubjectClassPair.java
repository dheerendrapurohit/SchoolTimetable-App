package com.example.school.entity;

import java.io.Serializable;

public class SubjectClassPair implements Serializable {
    private String subject;
    private String classes;

    public SubjectClassPair() {}

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    public SubjectClassPair(String subject, String classes) {
        this.subject = subject;
        this.classes = classes;
    }
}
