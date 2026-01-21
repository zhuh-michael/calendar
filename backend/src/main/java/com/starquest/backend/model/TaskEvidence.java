package com.starquest.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_evidence")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvidence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime = LocalDateTime.now();
}
