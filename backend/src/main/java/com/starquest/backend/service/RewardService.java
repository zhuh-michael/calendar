package com.starquest.backend.service;

import com.starquest.backend.model.Order;
import com.starquest.backend.model.Reward;
import com.starquest.backend.model.Transaction;
import com.starquest.backend.model.User;
import com.starquest.backend.repository.OrderRepository;
import com.starquest.backend.repository.RewardRepository;
import com.starquest.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;
    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public List<Reward> getAllActiveRewards() {
        return rewardRepository.findByActiveTrueOrderByIdDesc();
    }

    public List<Reward> getRewardsByType(Reward.RewardType type) {
        return rewardRepository.findByTypeAndActiveTrue(type);
    }

    public Reward getRewardById(Long rewardId) {
        return rewardRepository.findById(rewardId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    @Transactional
    public Reward createReward(Reward reward) {
        return rewardRepository.save(reward);
    }

    @Transactional
    public Reward updateReward(Long rewardId, Reward rewardDetails) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        reward.setName(rewardDetails.getName());
        reward.setCost(rewardDetails.getCost());
        reward.setStock(rewardDetails.getStock());
        reward.setType(rewardDetails.getType());
        reward.setDescription(rewardDetails.getDescription());
        reward.setImageUrl(rewardDetails.getImageUrl());
        reward.setActive(rewardDetails.getActive());

        return rewardRepository.save(reward);
    }

    @Transactional
    public void deleteReward(Long rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        reward.setActive(false);
        rewardRepository.save(reward);
    }

    @Transactional
    public Order purchaseReward(Long kidId, Long rewardId) {
        User kid = userService.getUserById(kidId);
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (!reward.getActive()) {
            throw new RuntimeException("商品已下架");
        }

        if (reward.getStock() <= 0) {
            throw new RuntimeException("商品库存不足");
        }

        if (kid.getStarBalance() < reward.getCost()) {
            throw new RuntimeException("星星余额不足");
        }

        // 扣除星星
        userService.updateStarBalance(kidId, -reward.getCost());

        // 减少库存
        reward.setStock(reward.getStock() - 1);
        rewardRepository.save(reward);

        // 创建订单
        Order order = new Order();
        order.setKidId(kidId);
        order.setRewardId(rewardId);
        order.setStatus(Order.OrderStatus.APPLIED);
        Order savedOrder = orderRepository.save(order);

        // 记录交易
        Transaction transaction = new Transaction();
        transaction.setUserId(kidId);
        transaction.setAmount(-reward.getCost());
        transaction.setReason("购买商品: " + reward.getName());
        transaction.setRelatedRewardId(rewardId);
        transaction.setRelatedOrderId(savedOrder.getId());
        transactionRepository.save(transaction);

        return savedOrder;
    }

    @Transactional
    public void deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (order.getStatus() != Order.OrderStatus.APPLIED) {
            throw new RuntimeException("订单状态不允许发货");
        }

        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setUpdateTime(java.time.LocalDateTime.now());
        orderRepository.save(order);
    }

    public List<Order> getOrdersByKid(Long kidId) {
        return orderRepository.findByKidIdOrderByCreateTimeDesc(kidId);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.APPLIED);
    }

    public java.util.List<com.starquest.backend.dto.PendingOrderDto> getPendingOrdersDetailed() {
        java.util.List<Order> orders = orderRepository.findByStatus(Order.OrderStatus.APPLIED);
        java.util.List<com.starquest.backend.dto.PendingOrderDto> out = new java.util.ArrayList<>();
        for (Order o : orders) {
            com.starquest.backend.dto.PendingOrderDto dto = new com.starquest.backend.dto.PendingOrderDto();
            dto.setId(o.getId());
            dto.setKidId(o.getKidId());
            try {
                User kid = userService.getUserById(o.getKidId());
                dto.setKidNickname(kid.getNickname() != null ? kid.getNickname() : kid.getUsername());
            } catch (Exception e) {
                dto.setKidNickname("未知");
            }
            dto.setRewardId(o.getRewardId());
            try {
                Reward r = rewardRepository.findById(o.getRewardId()).orElse(null);
                dto.setRewardName(r != null ? r.getName() : "已删除商品");
            } catch (Exception e) {
                dto.setRewardName("未知");
            }
            dto.setCreateTime(o.getCreateTime());
            dto.setStatus(o.getStatus().ordinal());
            out.add(dto);
        }
        return out;
    }

    public Long getPendingOrdersCount() {
        return orderRepository.countByKidIdAndStatus(0L, Order.OrderStatus.APPLIED); // 简化统计
    }
}
