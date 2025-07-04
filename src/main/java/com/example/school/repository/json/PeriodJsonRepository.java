package com.example.school.repository.json;

import com.example.school.entity.Period;
import org.springframework.stereotype.Repository;

@Repository
public class PeriodJsonRepository extends JsonRepository<Period> {
    public PeriodJsonRepository() {
        super("periods.json", Period.class);
    }
}
