package com.starquest.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "level_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer level;

    @Column(nullable = false)
    private String title;

    @Column(name = "required_exp", nullable = false)
    private Integer requiredExp;

    @Column(name = "avatar_frame")
    private String avatarFrame;
}

