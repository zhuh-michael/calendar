package com.starquest.backend.repository;

import com.starquest.backend.model.MoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MoodLogRepository extends JpaRepository<MoodLog, Long> {

    List<MoodLog> findByKidIdOrderByCreateTimeDesc(Long kidId);

    List<MoodLog> findByKidIdAndCreateTimeBetween(Long kidId, LocalDateTime start, LocalDateTime end);
}
