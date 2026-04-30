package com.smartqueue.worker_service.service;

import com.smartqueue.worker_service.config.RabbitMQConfig;
import com.smartqueue.worker_service.model.Task;
import com.smartqueue.worker_service.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskProcessorService {

    private final TaskRepository taskRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Listen to HIGH priority queue
    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE_HIGH)
    public void processHighPriorityTask(Task task) {
        log.info("⚡ Processing HIGH priority task: {}", task.getId());
        processTask(task, "HIGH");
    }

    // Listen to NORMAL priority queue
    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE_NORMAL)
    public void processNormalPriorityTask(Task task) {
        log.info("📋 Processing NORMAL priority task: {}", task.getId());
        processTask(task, "NORMAL");
    }

    private void processTask(Task task, String priority) {
        try {
            // Update status to PROCESSING
            task.setStatus("PROCESSING");
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);

            // Notify via WebSocket
            notifyUser(task, "PROCESSING");

            // Simulate actual task processing
            String result = simulateTaskProcessing(task);

            // Add delay to simulate real work
            Thread.sleep(priority.equals("HIGH") ? 1000 : 2000);

            // Update status to COMPLETED
            task.setStatus("COMPLETED");
            task.setResult(result);
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);

            // Notify completion via WebSocket
            notifyUser(task, "COMPLETED");

            log.info("✅ Task {} completed successfully", task.getId());

        } catch (Exception e) {
            // Update status to FAILED
            task.setStatus("FAILED");
            task.setResult("Error: " + e.getMessage());
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);

            notifyUser(task, "FAILED");
            log.error("❌ Task {} failed: {}", task.getId(), e.getMessage());
        }
    }

    private String simulateTaskProcessing(Task task) {
        return switch (task.getTaskType().toUpperCase()) {
            case "EMAIL" -> "Email sent successfully to: " + task.getPayload();
            case "REPORT" -> "Report generated with data: " + task.getPayload();
            case "NOTIFICATION" -> "Notification pushed to user: " + task.getUserId();
            case "EXPORT" -> "Data exported successfully: " + task.getPayload();
            default -> "Task processed: " + task.getPayload();
        };
    }

    private void notifyUser(Task task, String status) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/task-status/" + task.getUserId(),
                "Task #" + task.getId() + " is " + status
            );
        } catch (Exception e) {
            log.warn("WebSocket notification failed: {}", e.getMessage());
        }
    }
}