package com.example.finaltask;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class Task implements Serializable {
    private String documentId;
    private String taskName;
    private String taskDateTime;
    private String taskCategory;
    private long taskTimestamp; // Lưu thời gian dạng timestamp

    // Constructor mặc định (bắt buộc cho Firestore)
    public Task() {}

    // Constructor đầy đủ tham số
    public Task(String taskName, String taskDateTime, String taskCategory, long taskTimestamp) {
        this.taskName = taskName;
        this.taskDateTime = taskDateTime;
        this.taskCategory = taskCategory;
        this.taskTimestamp = taskTimestamp;
    }

    // Getter/Setter cho documentId
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Getter/Setter cho taskName
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    // Getter/Setter cho taskDateTime
    public String getTaskDateTime() {
        return taskDateTime;
    }

    public void setTaskDateTime(String taskDateTime) {
        this.taskDateTime = taskDateTime;
    }

    // Getter/Setter cho taskCategory
    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }

    // Getter/Setter cho taskTimestamp
    public long getTaskTimestamp() {
        return taskTimestamp;
    }

    public void setTaskTimestamp(long taskTimestamp) {
        this.taskTimestamp = taskTimestamp;
    }

    @Override
    public String toString() {
        return "Task{" +
                "documentId='" + documentId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", taskDateTime='" + taskDateTime + '\'' +
                ", taskCategory='" + taskCategory + '\'' +
                ", taskTimestamp=" + taskTimestamp +
                '}';
    }
}