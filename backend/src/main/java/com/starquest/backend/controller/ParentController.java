package com.starquest.backend.controller;

import com.starquest.backend.model.Transaction;
import com.starquest.backend.model.User;
import com.starquest.backend.repository.TransactionRepository;
import com.starquest.backend.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final UserService userService;
    private final TransactionRepository transactionRepository;

    @GetMapping("/kids")
    public ResponseEntity<java.util.List<User>> listKids() {
        return ResponseEntity.ok(userService.getKids());
    }

    @PostMapping("/kids/{kidId}/adjust")
    public ResponseEntity<Void> adjustKidBalance(@PathVariable Long kidId, @RequestBody AdjustRequest req) {
        // apply balance update
        userService.updateStarBalance(kidId, req.getAmount());

        // record transaction
        Transaction tx = new Transaction();
        tx.setUserId(kidId);
        tx.setAmount(req.getAmount());
        tx.setReason(req.getNote() == null ? "人工调整" : req.getNote());
        tx.setCreateTime(LocalDateTime.now());
        transactionRepository.save(tx);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/kids")
    public ResponseEntity<User> createKidAsParent(@RequestBody CreateKidRequest req) {
        User kid = userService.createUser(req.getUsername(), req.getPassword(), User.UserRole.KID);
        kid.setNickname(req.getNickname());
        userService.saveUser(kid);
        return ResponseEntity.ok(kid);
    }

    @PutMapping("/kids/{kidId}")
    public ResponseEntity<User> updateKid(@PathVariable Long kidId, @RequestBody UpdateKidRequest req) {
        User kid = userService.getUserById(kidId);
        if (req.getNickname() != null) kid.setNickname(req.getNickname());
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            kid.setPassword(com.starquest.backend.security.Sha256Util.sha256Hex(req.getPassword()));
        }
        if (req.getStarBalance() != null) kid.setStarBalance(req.getStarBalance());
        userService.saveUser(kid);
        return ResponseEntity.ok(kid);
    }

    @Data
    public static class CreateKidRequest {
        private String username;
        private String password;
        private String nickname;
    }

    @Data
    public static class UpdateKidRequest {
        private String nickname;
        private String password;
        private Integer starBalance;
    }
    @Data
    public static class AdjustRequest {
        private Integer amount;
        private String note;
    }
}


