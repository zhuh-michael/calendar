package com.starquest.backend.controller;

import com.starquest.backend.model.Order;
import com.starquest.backend.model.Reward;
import com.starquest.backend.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @GetMapping
    public ResponseEntity<List<Reward>> getAllRewards() {
        List<Reward> rewards = rewardService.getAllActiveRewards();
        return ResponseEntity.ok(rewards);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Reward>> getRewardsByType(@PathVariable Reward.RewardType type) {
        List<Reward> rewards = rewardService.getRewardsByType(type);
        return ResponseEntity.ok(rewards);
    }

    @PostMapping
    public ResponseEntity<Reward> createReward(@RequestBody Reward reward) {
        Reward createdReward = rewardService.createReward(reward);
        return ResponseEntity.ok(createdReward);
    }

    @PutMapping("/{rewardId}")
    public ResponseEntity<Reward> updateReward(@PathVariable Long rewardId, @RequestBody Reward reward) {
        Reward updatedReward = rewardService.updateReward(rewardId, reward);
        return ResponseEntity.ok(updatedReward);
    }

    @DeleteMapping("/{rewardId}")
    public ResponseEntity<Void> deleteReward(@PathVariable Long rewardId) {
        rewardService.deleteReward(rewardId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{rewardId}/purchase")
    public ResponseEntity<Order> purchaseReward(@PathVariable Long rewardId, @RequestParam Long kidId) {
        Order order = rewardService.purchaseReward(kidId, rewardId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/orders/{orderId}/deliver")
    public ResponseEntity<Void> deliverOrder(@PathVariable Long orderId) {
        rewardService.deliverOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending-orders")
    public ResponseEntity<java.util.List<com.starquest.backend.dto.PendingOrderDto>> getPendingOrders() {
        java.util.List<com.starquest.backend.dto.PendingOrderDto> orders = rewardService.getPendingOrdersDetailed();
        return ResponseEntity.ok(orders);
    }
}
