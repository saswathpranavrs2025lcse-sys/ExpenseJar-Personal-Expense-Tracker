package com.example.expensetracker.dto;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String userIdCode;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public UserResponse(Long id, String userIdCode, String name, String email, LocalDateTime createdAt) {
        this.id = id;
        this.userIdCode = userIdCode;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserIdCode() {
        return userIdCode;
    }

    public void setUserIdCode(String userIdCode) {
        this.userIdCode = userIdCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
