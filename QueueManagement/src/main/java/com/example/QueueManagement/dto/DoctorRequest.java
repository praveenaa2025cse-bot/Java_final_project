package com.example.QueueManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DoctorRequest(
        @NotBlank String name,
        @NotBlank String specialization,
        @NotNull @Positive Integer averageConsultationTime) {
}