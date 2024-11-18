package com.happy.observator.repository;

import java.time.LocalTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.happy.observator.model.S_Order;

public interface OrderRepositary extends JpaRepository<S_Order, String>{
    Optional<S_Order> findByTargetTime(LocalTime targetTime);
    Optional<S_Order> findByUserIdAndTargetTime(int userId, LocalTime targetTime);
}
