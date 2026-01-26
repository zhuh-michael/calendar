package com.starquest.backend.repository;

import com.starquest.backend.model.LevelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LevelConfigRepository extends JpaRepository<LevelConfig, Long> {
    Optional<LevelConfig> findByLevel(Integer level);
}

