package com.example.QueueManagement.service;

import com.example.QueueManagement.dto.PatientRequest;
import com.example.QueueManagement.entity.Patient;
import com.example.QueueManagement.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public Patient createPatient(PatientRequest request) {
        Patient patient = new Patient(request.name().trim(), request.phoneNumber().trim());
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));
    }

    @Transactional
    public Patient updatePatient(Long id, PatientRequest request) {
        Patient patient = getPatientById(id);
        patient.setName(request.name().trim());
        patient.setPhoneNumber(request.phoneNumber().trim());
        return patientRepository.save(patient);
    }

    @Transactional
    public void deletePatient(Long id) {
        patientRepository.delete(getPatientById(id));
    }
}