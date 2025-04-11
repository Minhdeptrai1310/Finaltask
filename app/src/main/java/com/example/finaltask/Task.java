package com.example.finaltask;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties // Thêm annotation cho Firestore
public class Task implements Serializable {
    private String documentId; // Thay thế taskId bằng documentId
    private String taskName;
    private String taskDateTime;
    private String taskCategory;
    private String userId;

    // Constructor mặc định (bắt buộc cho Firestore)
    public Task() {}

    // Constructor với 3 tham số (đã sửa lỗi logic)
    public Task(String taskName, String taskDateTime, String taskCategory) {
        this.taskName = taskName;
        this.taskDateTime = taskDateTime;
        this.taskCategory = taskCategory;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    // Getter/Setter cho documentId
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Các getter/setter khác giữ nguyên
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
}