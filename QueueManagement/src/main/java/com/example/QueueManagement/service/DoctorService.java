package com.example.QueueManagement.service;

import com.example.QueueManagement.dto.DoctorRequest;
import com.example.QueueManagement.entity.Doctor;
import com.example.QueueManagement.repository.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor createDoctor(DoctorRequest request) {
        Doctor doctor = new Doctor(
                request.name().trim(),
                request.specialization().trim(),
                request.averageConsultationTime());
        return createDoctor(doctor);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + id));
    }

    @Transactional
    public Doctor updateDoctor(Long id, Doctor doctor) {
        Doctor existingDoctor = getDoctorById(id);
        existingDoctor.setName(doctor.getName());
        existingDoctor.setSpecialization(doctor.getSpecialization());
        existingDoctor.setAverageConsultationTime(doctor.getAverageConsultationTime());
        return doctorRepository.save(existingDoctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
    }
}