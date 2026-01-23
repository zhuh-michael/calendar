package com.starquest.backend.repository;

import com.starquest.backend.model.Task;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByKidIdAndStartTimeBetween(Long kidId, LocalDateTime start, LocalDateTime end);

    List<Task> findByKidIdAndStatus(Long kidId, Task.TaskStatus status);

    List<Task> findByKidIdAndStatus(Long kidId, Task.TaskStatus status, Pageable pageable);

    List<Task> findByKidId(Long kidId);

    List<Task> findByKidId(Long kidId, Pageable pageable);

    List<Task> findByStatus(Task.TaskStatus status);

    List<Task> findByStatus(Task.TaskStatus status, Pageable pageable);

    List<Task> findByIsTemplateTrue();

    @Query("SELECT t FROM Task t WHERE t.kidId = ?1 AND DATE(t.startTime) = DATE(?2)")
    List<Task> findByKidIdAndDate(Long kidId, LocalDateTime date);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.kidId = ?1 AND t.status = ?2 AND DATE(t.startTime) = DATE(?3)")
    Long countByKidIdAndStatusAndDate(Long kidId, Task.TaskStatus status, LocalDateTime date);

    @Query("SELECT t FROM Task t WHERE " +
           "(:kidId IS NULL OR t.kidId = :kidId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:date IS NULL OR DATE(t.startTime) = :date)")
    List<Task> findTasksByConditions(@Param("kidId") Long kidId,
                                    @Param("status") Task.TaskStatus status,
                                    @Param("date") LocalDate date,
                                    Pageable pageable);
}
