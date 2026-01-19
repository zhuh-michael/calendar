package com.starquest.backend.controller;

import com.starquest.backend.dto.LoginRequest;
import com.starquest.backend.dto.LoginResponse;
import com.starquest.backend.model.User;
import com.starquest.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import com.starquest.backend.security.JwtUtil;
import com.starquest.backend.security.Sha256Util;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
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
        String token = jwtUtil.generateToken(user.getUsername());
        response.setToken(token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-kid")
    public ResponseEntity<User> createKid(@RequestParam String username,
                                          @RequestParam String password,
                                          @RequestParam String nickname) {
        User kid = userService.createUser(username, password, User.UserRole.KID);
        kid.setNickname(nickname);
        return ResponseEntity.ok(kid);
    }

    @GetMapping("/kids")
    public ResponseEntity<java.util.List<User>> getKids() {
        java.util.List<User> kids = userService.getKids();
        return ResponseEntity.ok(kids);
    }
}
