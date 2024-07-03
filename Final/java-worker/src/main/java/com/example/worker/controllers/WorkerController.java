package com.example.worker.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class WorkerController {

    private static final Logger logger = LoggerFactory.getLogger(WorkerController.class);
    private final List<String> workers = Arrays.asList("http://worker1:5001", "http://worker3:5003", "http://worker4:5004");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean isLeader = false;
    private String leaderUrl = null;

    @PostMapping("/submit_task")
    public Map<String, Object> submitTask(@RequestBody JsonNode taskJson) {
        logger.info("Received task: {}", taskJson);

        try {
            String clientId = taskJson.get("client_id").asText();
            String fileContent = taskJson.get("file_content").asText();
            String taskType = taskJson.get("task_type").asText();
            String keyword = taskJson.get("keyword").asText();
            int n = taskJson.get("n").asInt();

            logger.info("Processing task: {} for client {}", taskType, clientId);
            String result = processTask(fileContent, taskType, keyword, n);

            return Map.of("status", "Task processed successfully", "result", result);
        } catch (Exception e) {
            logger.error("Error processing task", e);
            return Map.of("status", "Error processing task", "error", e.getMessage());
        }
    }

    private String processTask(String fileContent, String taskType, String keyword, int n) {
        String result;
        switch (taskType) {
            case "word_count":
                result = String.valueOf(fileContent.split("\\s+").length);
                logger.info("Word count: {}", result);
                break;
            case "keyword_search":
                result = String.valueOf(fileContent.contains(keyword));
                logger.info("Keyword search: {}", result);
                break;
            case "keyword_repetition":
                result = String.valueOf(fileContent.split(keyword, -1).length - 1 >= n);
                logger.info("Keyword repetition: {}", result);
                break;
            default:
                result = "Invalid task type";
                logger.error("Invalid task type");
                break;
        }
        logger.info("Result for task: {}", result);
        return result;
    }

    @PostMapping("/heartbeat")
    public Map<String, String> heartbeat(@RequestBody Map<String, String> heartbeat) {
        String workerId = heartbeat.get("worker_id");
        logger.info("Received heartbeat from worker {}", workerId);
        return Map.of("status", "Heartbeat received");
    }

    @GetMapping("/is_leader")
    public Map<String, Object> isLeader() {
        logger.info("Checking leader status");
        return Map.of("is_leader", isLeader, "worker_id", "worker4");
    }

    @GetMapping("/elect_leader")
    public Map<String, String> electLeader() {
        isLeader = true;
        leaderUrl = "http://worker4:5004";
        logger.info("Worker 4 is elected as leader");
        return Map.of("status", "Worker 4 is the leader");
    }
}
