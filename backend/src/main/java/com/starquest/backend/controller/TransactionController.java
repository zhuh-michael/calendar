package com.starquest.backend.controller;

import com.starquest.backend.model.Transaction;
import com.starquest.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<Integer> getUserBalance(@PathVariable Long userId) {
        Integer balance = transactionRepository.getTotalBalance(userId);
        return ResponseEntity.ok(balance != null ? balance : 0);
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(userId, start, end);
        return ResponseEntity.ok(transactions);
    }
}
