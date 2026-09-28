package com.example.QueueManagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens", uniqueConstraints = @UniqueConstraint(
        columnNames = {"doctor_id", "token_date", "token_number"}))
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "token_date", nullable = false)
    private LocalDate tokenDate;

    @Column(name = "token_number", nullable = false)
    private Integer tokenNumber;

    @Column(nullable = false)
    private boolean priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "served_at")
    private LocalDateTime servedAt;

    protected Token() {
    }

    public Token(Doctor doctor, Patient patient, LocalDate tokenDate, Integer tokenNumber, boolean priority, LocalDateTime generatedAt) {
        this.doctor = doctor;
        this.patient = patient;
        this.tokenDate = tokenDate;
        this.tokenNumber = tokenNumber;
        this.priority = priority;
        this.generatedAt = generatedAt;
        this.status = TokenStatus.WAITING;
    }

    public Long getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public LocalDate getTokenDate() {
        return tokenDate;
    }

    public Integer getTokenNumber() {
        return tokenNumber;
    }

    public boolean isPriority() {
        return priority;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getServedAt() {
        return servedAt;
    }

    public void setServedAt(LocalDateTime servedAt) {
        this.servedAt = servedAt;
    }
}