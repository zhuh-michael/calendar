package com.starquest.backend.controller;

import com.starquest.backend.dto.CreateKidRequest;
import com.starquest.backend.dto.LoginRequest;
import com.starquest.backend.dto.LoginResponse;
import com.starquest.backend.model.User;
import com.starquest.backend.service.RpgService;
import com.starquest.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import com.starquest.backend.security.JwtUtil;
import com.starquest.backend.security.Sha256Util;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RpgService rpgService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Optional<User> optUser = userService.findByUsername(request.getUsername());
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = optUser.get();
        String stored = user.getPassword();
        // compute sha256 of provided password
        String sha256 = Sha256Util.sha256Hex(request.getPassword());
        boolean ok = false;
        if (stored != null && stored.equals(sha256)) {
            ok = true;
        } else {
            // fallback: if stored looks like bcrypt, try bcrypt match
            if (stored != null && stored.startsWith("$2a$")) {
                if (passwordEncoder.matches(request.getPassword(), stored)) {
                    // migrate user password to sha256 for future logins
                    user.setPassword(sha256);
                    // save migration
                    userService.saveUser(user);
                    ok = true;
                }
            }
        }

        if (!ok) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getNickname(),
                user.getStarBalance()
        );
        response.setToken(jwtUtil.generateToken(user.getUsername()));

        // 设置 RPG 字段
        response.setExp(user.getExp());
        response.setLevel(user.getLevel());
        response.setLevelTitle(user.getLevelTitle());
        response.setStreakDays(user.getStreakDays());
        response.setAvatarFrame(rpgService.getAvatarFrame(user.getLevel()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-kid")
    public ResponseEntity<User> createKid(@RequestBody CreateKidRequest request) {
        User kid = userService.createUser(request.getUsername(), request.getPassword(), User.UserRole.KID, request.getNickname());
        return ResponseEntity.ok(kid);
    }

    @GetMapping("/kids")
    public ResponseEntity<java.util.List<User>> getKids() {
        java.util.List<User> kids = userService.getKids();
        return ResponseEntity.ok(kids);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optUser = userService.findByUsername(username);
        if (optUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optUser.get());
    }

    /**
     * 每日打卡
     * POST /api/auth/checkin
     */
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optUser = userService.findByUsername(username);

        if (optUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optUser.get();
        RpgService.CheckInResult result = rpgService.checkIn(user.getId());

        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户 RPG 信息（等级进度）
     * GET /api/auth/rpg-info
     */
    @GetMapping("/rpg-info")
    public ResponseEntity<?> getRpgInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optUser = userService.findByUsername(username);

        if (optUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optUser.get();

        // 计算等级进度
        int currentLevelExp = rpgService.getLevelBaseExp(user.getLevel());
        int nextLevelExp = rpgService.getNextLevelBaseExp(user.getLevel());
        int progressExp = user.getExp() - currentLevelExp;
        int requiredExp = nextLevelExp - currentLevelExp;
        int progressPercent = requiredExp > 0 ? (int) Math.round((double) progressExp / requiredExp * 100) : 100;

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("exp", user.getExp());
        response.put("level", user.getLevel());
        response.put("levelTitle", user.getLevelTitle());
        response.put("streakDays", user.getStreakDays());
        response.put("lastCheckinDate", user.getLastCheckinDate() != null ? user.getLastCheckinDate().toString() : null);
        response.put("avatarFrame", rpgService.getAvatarFrame(user.getLevel()));
        response.put("currentLevelExp", currentLevelExp);
        response.put("nextLevelExp", nextLevelExp);
        response.put("progressPercent", progressPercent);

        return ResponseEntity.ok(response);
    }
}
