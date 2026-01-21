package com.starquest.backend.repository;

import com.starquest.backend.model.TaskEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskEvidenceRepository extends JpaRepository<TaskEvidence, Long> {
    List<TaskEvidence> findByTaskIdOrderByUploadTimeDesc(Long taskId);
    void deleteByTaskId(Long taskId);
}
