package com.starquest.backend.service;

import com.starquest.backend.model.User;
import com.starquest.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // @Transactional
    // public User createUser(String username, String password, User.UserRole role) {
    //     return createUser(username, password, role, null);
    // }

    @Transactional
    public User createUser(String username, String password, User.UserRole role, String nickname) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        // store SHA-256 hex of password for simplified auth across clients
        user.setPassword(com.starquest.backend.security.Sha256Util.sha256Hex(password));
        user.setRole(role);
        user.setStarBalance(0);
        if (nickname != null) {
            user.setNickname(nickname);
        }

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getKids() {
        return userRepository.findByRole(User.UserRole.KID);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Transactional
    public void updateStarBalance(Long userId, Integer amount) {
        User user = getUserById(userId);
        user.setStarBalance(user.getStarBalance() + amount);
        userRepository.save(user);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    @Transactional
    public void initializeDefaultAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            createUser("admin", "password", User.UserRole.PARENT, "管理员");
        }
    }
}
