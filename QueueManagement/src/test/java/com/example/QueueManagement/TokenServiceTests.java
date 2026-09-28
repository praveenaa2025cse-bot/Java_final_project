package com.example.QueueManagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.QueueManagement.dto.TokenRequest;
import com.example.QueueManagement.dto.TokenResponse;
import com.example.QueueManagement.entity.Doctor;
import com.example.QueueManagement.entity.Patient;
import com.example.QueueManagement.entity.Token;
import com.example.QueueManagement.entity.TokenStatus;
import com.example.QueueManagement.repository.DoctorRepository;
import com.example.QueueManagement.repository.PatientRepository;
import com.example.QueueManagement.repository.TokenRepository;
import com.example.QueueManagement.service.TokenService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TokenServiceTests {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    void assignsDailyNumbersHonorsPriorityAndAdvancesServingOrder() {
        Doctor doctor = doctorRepository.save(new Doctor("Queue Test Doctor", "General", 10));
        Patient firstPatient = patientRepository.save(new Patient("Queue Test One", "5550000101"));
        Patient secondPatient = patientRepository.save(new Patient("Queue Test Two", "5550000102"));
        Patient priorityPatient = patientRepository.save(new Patient("Queue Test Priority", "5550000103"));

        tokenRepository.save(new Token(doctor, firstPatient, LocalDate.now().minusDays(1), 99, false, LocalDateTime.now()));

        TokenResponse first = tokenService.createToken(new TokenRequest(doctor.getId(), firstPatient.getId(), false));
        TokenResponse second = tokenService.createToken(new TokenRequest(doctor.getId(), secondPatient.getId(), false));
        TokenResponse priority = tokenService.createToken(new TokenRequest(doctor.getId(), priorityPatient.getId(), true));

        assertThat(first.tokenNumber()).isEqualTo(1);
        assertThat(second.tokenNumber()).isEqualTo(2);
        
        assertThat(tokenService.getWaitEstimate(second.id()).estimatedWaitTime()).isEqualTo(20);
        
        assertThat(priority.tokenNumber()).isEqualTo(3);
        assertThat(tokenService.getWaitEstimate(priority.id()).estimatedWaitTime()).isZero();

        assertThatThrownBy(() -> tokenService.createToken(
                new TokenRequest(doctor.getId(), secondPatient.getId(), true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Priority token already exists");

        TokenResponse nowServing = tokenService.advanceQueue(doctor.getId());
        assertThat(nowServing.tokenNumber()).isEqualTo(3);
        assertThat(nowServing.status()).isEqualTo(TokenStatus.SERVING);

        assertThatThrownBy(() -> tokenService.createToken(
                new TokenRequest(doctor.getId(), secondPatient.getId(), true)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(tokenService.advanceQueue(doctor.getId()).tokenNumber()).isEqualTo(1);
        assertThat(tokenService.advanceQueue(doctor.getId()).tokenNumber()).isEqualTo(2);
        
        assertThatThrownBy(() -> tokenService.advanceQueue(doctor.getId()))
                .isInstanceOf(IllegalArgumentException.class);

        List<TokenResponse> history = tokenService.getDailyHistory(doctor.getId(), LocalDate.now());
        assertThat(history).extracting(TokenResponse::tokenNumber).containsExactly(1, 2, 3);
        assertThat(history).extracting(TokenResponse::status)
                .containsOnly(TokenStatus.COMPLETED);
    }

    @Test
    void missingDoctorReturnsNotFoundError() {
        assertThatThrownBy(() -> tokenService.getDailyHistory(Long.MAX_VALUE, LocalDate.now()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Doctor not found");
    }
}