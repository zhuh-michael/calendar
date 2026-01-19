package com.starquest.backend.repository;

import com.starquest.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByKidIdOrderByCreateTimeDesc(Long kidId);

    List<Order> findByStatus(Order.OrderStatus status);

    Long countByKidIdAndStatus(Long kidId, Order.OrderStatus status);
}
