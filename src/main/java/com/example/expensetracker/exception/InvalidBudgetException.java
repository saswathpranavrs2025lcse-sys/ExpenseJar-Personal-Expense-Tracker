package com.example.expensetracker.exception;

public class InvalidBudgetException extends RuntimeException {
    public InvalidBudgetException(String message) {
        super(message);
    }
}
