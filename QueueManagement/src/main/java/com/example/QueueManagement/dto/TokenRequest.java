package com.example.QueueManagement.dto;

import jakarta.validation.constraints.NotNull;

public record TokenRequest(
        @NotNull(message = "doctorId must not be null") Long doctorId,
        @NotNull(message = "patientId must not be null") Long patientId,
        boolean priority) {
}