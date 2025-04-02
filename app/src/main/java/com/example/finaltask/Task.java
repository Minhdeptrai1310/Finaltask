package com.example.finaltask;

import java.io.Serializable;

public class Task implements Serializable {  // Thêm Serializable ở đây
    private String taskId;
    private String taskName;
    private String taskDateTime;
    private String taskCategory;

    // Constructor mặc định
    public Task() {}

    // Constructor với 4 tham số
    public Task(String taskName, String taskDateTime, String taskCategory) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskDateTime = taskDateTime;
        this.taskCategory = taskCategory;
    }

    // Getter và Setter cho các trường
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDateTime() {
        return taskDateTime;
    }

    public void setTaskDateTime(String taskDateTime) {
        this.taskDateTime = taskDateTime;
    }

    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
