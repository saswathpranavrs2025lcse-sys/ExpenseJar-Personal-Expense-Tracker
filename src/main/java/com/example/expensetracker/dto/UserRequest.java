package com.example.expensetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRequest {

    @NotBlank(message = "User name cannot be blank")
    private String name;

    @NotBlank(message = "User email cannot be blank")
    @Email(message = "User email must be a valid email address")
    private String email;

    public UserRequest() {
    }

    public UserRequest(String name, String email) {
        this.name = name;
        this.email = email;
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
}
