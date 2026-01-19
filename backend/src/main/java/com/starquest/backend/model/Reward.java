package com.starquest.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "rewards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer cost;

    @Column(nullable = false)
    private Integer stock = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardType type = RewardType.ITEM;

    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private Boolean active = true;

    public enum RewardType {
        ITEM,       // 实物奖
        VIRTUAL,    // 虚拟权益
        BLINDBOX    // 盲盒券
    }
}
