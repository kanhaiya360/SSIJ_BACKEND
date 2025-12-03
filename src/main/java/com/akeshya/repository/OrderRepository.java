package com.akeshya.repository;

import com.akeshya.entity.Order;
import com.akeshya.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserId(UUID userId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);
	List<Order> findAllByOrderByCreatedAtDesc();
}