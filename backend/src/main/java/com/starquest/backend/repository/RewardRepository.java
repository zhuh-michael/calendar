package com.starquest.backend.repository;

import com.starquest.backend.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByActiveTrueOrderByIdDesc();

    List<Reward> findByTypeAndActiveTrue(Reward.RewardType type);
}
