package com.example.expensetracker.exception;

public class InvalidExpenseException extends RuntimeException {
    public InvalidExpenseException(String message) {
        super(message);
    }
}
