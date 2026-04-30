package com.smartqueue.task_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String taskType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Integer priority = 1;

    private String status = "PENDING";

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String result;
}