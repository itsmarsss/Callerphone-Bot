package com.itsmarsss.callerphone.safety;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {
    void save(Report report);

    Optional<Report> findById(String id);

    List<Report> findOpen(int limit);
}
