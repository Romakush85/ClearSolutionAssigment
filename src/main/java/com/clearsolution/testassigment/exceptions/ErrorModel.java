package com.clearsolution.testassigment.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorModel {
    private String message;
    private LocalDateTime timestamp;
}
