package com.happy.observator.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.happy.observator.repository.UserRepositary;
import com.happy.observator.model.User;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepositary userRepositary;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepositary.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public Optional<User> findByUsername(String username) {
        return userRepositary.findByUsername(username);
    }

    public boolean userExists(String username) {
        return userRepositary.findByUsername(username).isPresent();
    }

    public void saveUser(String username, String password) {
        if (userRepositary.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }

        User user = new User(username, passwordEncoder.encode(password));
        userRepositary.save(user);
        logger.info("User '{}' saved successfully!", username);
    }

    public void saveUser(String username, String password, String email, String phoneNumber, int emailAgreed) {
        if (userRepositary.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }

        User user = new User(username, passwordEncoder.encode(password), email, phoneNumber);
        user.setEmailAgreed(emailAgreed);
        userRepositary.save(user);
        logger.info("User '{}' with email '{}' saved successfully!", username, email);
    }

    public void updateUserInfo(String username, String upbitAccessKey, String upbitSecretKey, String email) {
        User user = userRepositary.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        user.setEmail(email);
        user.setUpbitAccessKey(upbitAccessKey);
        user.setUpbitSecretKey(upbitSecretKey);
        boolean keysSet = !upbitAccessKey.isBlank() && !upbitSecretKey.isBlank();
        user.setUpbitKeysSet(keysSet);

        userRepositary.save(user);
        logger.info("Upbit keys updated for user '{}'. Keys set: {}", username, keysSet);
    }

    public boolean existsByUsername(String username) {
        return userRepositary.existsByUsername(username);
    }
}
