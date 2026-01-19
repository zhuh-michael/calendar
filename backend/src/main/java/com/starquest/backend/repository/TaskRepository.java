package com.starquest.backend.repository;

import com.starquest.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByKidIdAndStartTimeBetween(Long kidId, LocalDateTime start, LocalDateTime end);

    List<Task> findByKidIdAndStatus(Long kidId, Task.TaskStatus status);

    List<Task> findByIsTemplateTrue();

    @Query("SELECT t FROM Task t WHERE t.kidId = ?1 AND DATE(t.startTime) = DATE(?2)")
    List<Task> findByKidIdAndDate(Long kidId, LocalDateTime date);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.kidId = ?1 AND t.status = ?2 AND DATE(t.startTime) = DATE(?3)")
    Long countByKidIdAndStatusAndDate(Long kidId, Task.TaskStatus status, LocalDateTime date);
}
