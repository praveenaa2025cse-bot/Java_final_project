package com.example.QueueManagement.dto;

public record WaitTimeResponse(
        Integer tokenNumber,
        int tokensAhead,
        int averageConsultationTime,
        int estimatedWaitTime
) {}
