package com.example.finaltask;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.io.Serializable;
import java.util.List;

@IgnoreExtraProperties
public class Task implements Serializable {
    private String documentId;
    private String taskName;
    private String taskDateTime;
    private String taskCategory;
    private String userId; // ID người tạo công việc
    private List<String> sharedWith; // Danh sách email được chia sẻ
    private List<String> sharedWithOrOwner; // Kết hợp userId và email để lọc hiển thị

    // Constructor mặc định (bắt buộc cho Firestore)
    public Task() {}

    public Task(String taskName, String taskDateTime, String taskCategory) {
        this.taskName = taskName;
        this.taskDateTime = taskDateTime;
        this.taskCategory = taskCategory;
    }

    // documentId
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // taskName
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    // taskDateTime
    public String getTaskDateTime() {
        return taskDateTime;
    }

    public void setTaskDateTime(String taskDateTime) {
        this.taskDateTime = taskDateTime;
    }

    // taskCategory
    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }

    // userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // sharedWith
    public List<String> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<String> sharedWith) {
        this.sharedWith = sharedWith;
    }

    // sharedWithOrOwner
    public List<String> getSharedWithOrOwner() {
        return sharedWithOrOwner;
    }

    public void setSharedWithOrOwner(List<String> sharedWithOrOwner) {
        this.sharedWithOrOwner = sharedWithOrOwner;
    }
}
