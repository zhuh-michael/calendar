package com.starquest.backend.repository;

import com.starquest.backend.model.LuckyDrawRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LuckyDrawRecordRepository extends JpaRepository<LuckyDrawRecord, Long> {

    List<LuckyDrawRecord> findByKidIdOrderByCreateTimeDesc(Long kidId);
}
