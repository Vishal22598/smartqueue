package com.smartqueue.task_service.service;

import com.smartqueue.task_service.config.RabbitMQConfig;
import com.smartqueue.task_service.model.Task;
import com.smartqueue.task_service.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    // Rate limit — max 5 requests per minute per user
    private static final int MAX_REQUESTS = 5;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public Task submitTask(Task task) {

        // Check rate limit
        String redisKey = RATE_LIMIT_PREFIX + task.getUserId();
        String requestCount = redisTemplate.opsForValue().get(redisKey);

        if (requestCount != null && Integer.parseInt(requestCount) >= MAX_REQUESTS) {
            throw new RuntimeException("Rate limit exceeded! Max "
                    + MAX_REQUESTS + " requests per minute allowed.");
        }

        // Increment request count in Redis (expires in 1 minute)
        if (requestCount == null) {
            redisTemplate.opsForValue().set(redisKey, "1", 1, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().increment(redisKey);
        }

        // Save task to DB
        task.setStatus("PENDING");
        Task savedTask = taskRepository.save(task);

        // Route to correct queue based on priority
        String routingKey = task.getPriority() >= 2
                ? RabbitMQConfig.ROUTING_KEY_HIGH
                : RabbitMQConfig.ROUTING_KEY_NORMAL;

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TASK_EXCHANGE,
                routingKey,
                savedTask
        );

        log.info("Task {} submitted to {} queue", savedTask.getId(), routingKey);
        return savedTask;
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    public List<Task> getTasksByUser(String userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
}