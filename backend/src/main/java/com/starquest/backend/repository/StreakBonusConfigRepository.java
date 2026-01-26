package com.starquest.backend.repository;

import com.starquest.backend.model.StreakBonusConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StreakBonusConfigRepository extends JpaRepository<StreakBonusConfig, Long> {
    Optional<StreakBonusConfig> findByStreakDays(Integer streakDays);
}

