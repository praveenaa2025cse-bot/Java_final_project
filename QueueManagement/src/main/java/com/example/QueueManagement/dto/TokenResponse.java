package com.example.QueueManagement.dto;

import com.example.QueueManagement.entity.TokenStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TokenResponse(
        Long id,
        Long doctorId,
        Long patientId,
        LocalDate tokenDate,
        Integer tokenNumber,
        boolean priority,
        TokenStatus status,
        LocalDateTime generatedAt,
        LocalDateTime servedAt
) {}