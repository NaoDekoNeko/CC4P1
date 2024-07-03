package com.example.worker.controllers;

import com.example.worker.models.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class WorkerController {

    private static final Logger logger = LoggerFactory.getLogger(WorkerController.class);

    private boolean isLeader = false;
    private String leaderUrl = null;
    private final String workerId = System.getenv("WORKER_ID");
    private final List<String> workers = Arrays.asList("http://worker1:5001", "http://worker2:5002", "http://worker3:5003", "http://worker4:5004");

    @PostMapping("/submit_task")
    public ResponseEntity<Map<String, Object>> submitTask(@RequestBody Task task) {
        logger.info("Received task: {}", task);
        Map<String, Object> result = processTask(task);
        return ResponseEntity.ok(Map.of("status", "Task processed successfully", "result", result));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, String>> heartbeat(@RequestBody Map<String, String> body) {
        String leaderId = body.get("worker_id");
        logger.info("Received heartbeat from worker {}", leaderId);
        if (!leaderId.equals(workerId)) {
            isLeader = false;
            leaderUrl = "http://worker" + leaderId + ":" + System.getenv("PORT");
        } else {
            logger.info("Worker {} is the leader", workerId);
        }
        return ResponseEntity.ok(Map.of("status", "Heartbeat received"));
    }

    @GetMapping("/is_leader")
    public ResponseEntity<Map<String, Object>> getLeaderStatus() {
        logger.info("Checking leader status for worker {}", workerId);
        return ResponseEntity.ok(Map.of("is_leader", isLeader, "worker_id", workerId));
    }

    private Map<String, Object> processTask(Task task) {
        logger.info("Processing task: {} for client {}", task.getTaskType(), task.getClientId());
        String fileContent = task.getFileContent();
        String taskType = task.getTaskType();
        String keyword = task.getKeyword();
        Integer n = task.getN();
        Object result;

        switch (taskType) {
            case "word_count":
                result = keyword != null ? fileContent.split(keyword).length - 1 : fileContent.split("\\s+").length;
                break;
            case "keyword_search":
                result = fileContent.contains(keyword);
                break;
            case "keyword_repetition":
                result = fileContent.split(keyword).length - 1 >= n;
                break;
            default:
                result = "Invalid task type";
                break;
        }

        logger.info("Result for client {} is {}", task.getClientId(), result);
        return Map.of("result", result);
    }
}