package com.starquest.backend.controller;

import com.starquest.backend.model.Transaction;
import com.starquest.backend.model.User;
import com.starquest.backend.repository.TransactionRepository;
import com.starquest.backend.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import com.starquest.backend.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final FileStorageService fileStorageService;

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

    // Legacy JSON create/update endpoints removed. Use multipart endpoints:
    // POST /api/auth/create-kid (multipart) for creation
    // PUT  /api/parents/kids/{kidId} (multipart) for update

    // 支持 multipart 更新（包含 avatar 文件）
    @PutMapping(value = "/kids/{kidId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateKidMultipart(
            @PathVariable Long kidId,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "starBalance", required = false) Integer starBalance,
            @RequestParam(value = "avatar", required = false) org.springframework.web.multipart.MultipartFile avatar,
            @RequestParam(value = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile,
            @RequestParam(value = "removeExistingImage", required = false) Boolean removeExistingImage
    ) {
        User kid = userService.getUserById(kidId);
        if (nickname != null) kid.setNickname(nickname);
        if (password != null && !password.isEmpty()) {
            kid.setPassword(com.starquest.backend.security.Sha256Util.sha256Hex(password));
        }
        if (starBalance != null) kid.setStarBalance(starBalance);
        org.springframework.web.multipart.MultipartFile fileToSave = (avatar != null && !avatar.isEmpty()) ? avatar : imageFile;
        if (fileToSave != null && !fileToSave.isEmpty()) {
            String avatarUrl = fileStorageService.saveImage(fileToSave);
            kid.setAvatar(avatarUrl);
        }
        // handle explicit removal of existing image
        if ((removeExistingImage != null && removeExistingImage) && fileToSave == null) {
            kid.setAvatar(null);
        }
        userService.saveUser(kid);
        return ResponseEntity.ok(kid);
    }

    @DeleteMapping("/kids/{kidId}")
    public ResponseEntity<Void> deleteKid(@PathVariable Long kidId) {
        User kid = userService.getUserById(kidId);
        if (!kid.getRole().equals(User.UserRole.KID)) {
            throw new RuntimeException("只能删除孩子账号");
        }
        userService.deleteUser(kidId);
        return ResponseEntity.ok().build();
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


