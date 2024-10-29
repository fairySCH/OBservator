package com.happy.observator.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.happy.observator.repository.UserRepositary;
import com.happy.observator.model.User;

@Service
public class UserService implements UserDetailsService{

    @Autowired
    private UserRepositary userRepositary;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepositary.findByUsername(username);
        User user = optionalUser.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .roles("USER")
            .build();
    }

    public Optional<User> findByUsername(String username) {
        return userRepositary.findByUsername(username);
    }

    public void saveUser(String username, String password){
        if(userRepositary.findByUsername(username).isPresent()){
            throw new IllegalArgumentException("Username already taken");
        }
        User user = new User(username, passwordEncoder.encode(password));
        userRepositary.save(user);
    }

    public void updateUpbitKeys(String username, String upbitAccessKey, String upbitSecretKey) {
        User user = userRepositary.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUpbitAccessKey(upbitAccessKey);
        user.setUpbitSecretKey(upbitSecretKey);
        boolean keysSet = !upbitAccessKey.isBlank() && !upbitSecretKey.isBlank();
        user.setUpbitKeysSet(keysSet);
        userRepositary.save(user);
    }

}
