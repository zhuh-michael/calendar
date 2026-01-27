package com.starquest.backend.controller;

import com.starquest.backend.model.Reward;
import com.starquest.backend.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @Value("${app.upload.reward-image-path:uploads/rewards/}")
    private String rewardImagePath;

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

    /**
     * 创建商品（支持图片上传）
     * 使用 multipart/form-data 格式，同时接收图片和商品信息
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Reward> createReward(
            @RequestParam("name") String name,
            @RequestParam("cost") Integer cost,
            @RequestParam("stock") Integer stock,
            @RequestParam("type") Reward.RewardType type,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "active", defaultValue = "true") Boolean active) {

        // 处理图片上传
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        Reward reward = new Reward();
        reward.setName(name);
        reward.setCost(cost);
        reward.setStock(stock);
        reward.setType(type);
        reward.setDescription(description);
        reward.setImageUrl(imageUrl);
        reward.setActive(active);

        Reward createdReward = rewardService.createReward(reward);
        return ResponseEntity.ok(createdReward);
    }

    /**
     * 更新商品（支持图片上传）
     */
    @PutMapping(value = "/{rewardId}", consumes = "multipart/form-data")
    public ResponseEntity<Reward> updateReward(
            @PathVariable Long rewardId,
            @RequestParam("name") String name,
            @RequestParam("cost") Integer cost,
            @RequestParam("stock") Integer stock,
            @RequestParam("type") Reward.RewardType type,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "active", defaultValue = "true") Boolean active,
            @RequestParam(value = "keepExistingImage", defaultValue = "false") Boolean keepExistingImage) {

        // 处理图片上传
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        } else if (keepExistingImage) {
            // 保留现有图片，需要从数据库获取
            Reward existing = rewardService.getRewardById(rewardId);
            imageUrl = existing.getImageUrl();
        }

        Reward rewardDetails = new Reward();
        rewardDetails.setName(name);
        rewardDetails.setCost(cost);
        rewardDetails.setStock(stock);
        rewardDetails.setType(type);
        rewardDetails.setDescription(description);
        rewardDetails.setImageUrl(imageUrl);
        rewardDetails.setActive(active);

        Reward updatedReward = rewardService.updateReward(rewardId, rewardDetails);
        return ResponseEntity.ok(updatedReward);
    }

    /**
     * 获取单个商品详情
     */
    @GetMapping("/{rewardId}")
    public ResponseEntity<Reward> getReward(@PathVariable Long rewardId) {
        Reward reward = rewardService.getRewardById(rewardId);
        return ResponseEntity.ok(reward);
    }

    /**
     * 获取待处理订单（管理员视图） - 为兼容前端请求 /api/rewards/pending-orders
     */
    @GetMapping("/pending-orders")
    public ResponseEntity<java.util.List<com.starquest.backend.dto.PendingOrderDto>> getPendingOrdersDetailed() {
        java.util.List<com.starquest.backend.dto.PendingOrderDto> list = rewardService.getPendingOrdersDetailed();
        return ResponseEntity.ok(list);
    }

    /**
     * 私有方法：处理图片上传
     */
    private String uploadImage(MultipartFile file) {
        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("只支持图片文件");
            }

            // 按日期分文件夹存储
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 构建完整路径（与 TaskController 保持一致）
            String filePath = rewardImagePath + today + "/" + fileName;
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);

            // 确保父目录存在
            java.nio.file.Files.createDirectories(path.getParent());

            // 保存文件（使用 write，与 TaskController 一致）
            java.nio.file.Files.write(path, file.getBytes());

            // 返回图片URL（相对于 uploads 目录）
            return "uploads/rewards/" + today + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }
}
