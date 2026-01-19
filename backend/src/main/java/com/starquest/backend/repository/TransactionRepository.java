package com.starquest.backend.repository;

import com.starquest.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByCreateTimeDesc(Long userId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = ?1")
    Integer getTotalBalance(Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.userId = ?1 AND t.createTime BETWEEN ?2 AND ?3 ORDER BY t.createTime DESC")
    List<Transaction> findByUserIdAndDateRange(Long userId, LocalDateTime start, LocalDateTime end);
}
