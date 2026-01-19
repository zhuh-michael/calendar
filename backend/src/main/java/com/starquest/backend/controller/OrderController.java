package com.starquest.backend.controller;

import com.starquest.backend.model.Order;
import com.starquest.backend.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final RewardService rewardService;

    @GetMapping("/kid/{kidId}")
    public ResponseEntity<List<Order>> getOrdersByKid(@PathVariable Long kidId) {
        List<Order> orders = rewardService.getOrdersByKid(kidId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        List<Order> orders = rewardService.getPendingOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending/count")
    public ResponseEntity<Long> getPendingOrdersCount() {
        Long count = rewardService.getPendingOrdersCount();
        return ResponseEntity.ok(count);
    }
}
