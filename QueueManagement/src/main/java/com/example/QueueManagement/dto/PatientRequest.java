package com.example.QueueManagement.dto;

import jakarta.validation.constraints.NotBlank;

public record PatientRequest(
        @NotBlank String name,
        @NotBlank String phoneNumber) {
}