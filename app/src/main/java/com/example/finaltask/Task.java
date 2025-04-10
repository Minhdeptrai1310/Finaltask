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
    private String userId;
    private List<String> sharedWith;
    private List<String> sharedWithOrOwner; // ✅ Thêm dòng này

    public Task() {}

    public Task(String taskName, String taskDateTime, String taskCategory) {
        this.taskName = taskName;
        this.taskDateTime = taskDateTime;
        this.taskCategory = taskCategory;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<String> sharedWith) {
        this.sharedWith = sharedWith;
    }

    // ✅ Thêm getter và setter cho sharedWithOrOwner
    public List<String> getSharedWithOrOwner() {
        return sharedWithOrOwner;
    }

    public void setSharedWithOrOwner(List<String> sharedWithOrOwner) {
        this.sharedWithOrOwner = sharedWithOrOwner;
    }
}
