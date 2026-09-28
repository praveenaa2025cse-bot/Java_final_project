package com.example.QueueManagement.config;

import com.example.QueueManagement.entity.Doctor;
import com.example.QueueManagement.entity.Patient;
import com.example.QueueManagement.repository.DoctorRepository;
import com.example.QueueManagement.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(DoctorRepository doctorRepo, PatientRepository patientRepo) {
        return args -> {
            if (doctorRepo.count() == 0) {
                doctorRepo.save(new Doctor("Dr. John Watson", "General Physician", 15));
                doctorRepo.save(new Doctor("Dr. Sarah Smith", "Cardiology", 20));
            }
            if (patientRepo.count() == 0) {
                patientRepo.save(new Patient("Alice (Patient 1)", "9876543210"));
                patientRepo.save(new Patient("Bob (Patient 2)", "8765432109"));
                patientRepo.save(new Patient("Charlie (Patient 3)", "7654321098"));
                patientRepo.save(new Patient("David (Patient 4)", "6543210987"));
            }
        };
    }
}
