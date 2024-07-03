package com.example.worker.models;

public class Task {
    private String clientId;
    private String fileContent;
    private String taskType;
    private String keyword;
    private Integer n;

    // Getters y setters

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    @Override
    public String toString() {
        return "Task{" +
                "clientId='" + clientId + '\'' +
                ", fileContent='" + fileContent + '\'' +
                ", taskType='" + taskType + '\'' +
                ", keyword='" + keyword + '\'' +
                ", n=" + n +
                '}';
    }
}
