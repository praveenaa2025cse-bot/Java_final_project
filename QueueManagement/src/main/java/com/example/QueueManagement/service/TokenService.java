package com.example.QueueManagement.service;

import com.example.QueueManagement.dto.TokenRequest;
import com.example.QueueManagement.dto.TokenResponse;
import com.example.QueueManagement.dto.WaitTimeResponse;
import com.example.QueueManagement.entity.Doctor;
import com.example.QueueManagement.entity.Patient;
import com.example.QueueManagement.entity.Token;
import com.example.QueueManagement.entity.TokenStatus;
import com.example.QueueManagement.repository.DoctorRepository;
import com.example.QueueManagement.repository.PatientRepository;
import com.example.QueueManagement.repository.TokenRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {

    private static final List<TokenStatus> ACTIVE_PRIORITY_STATUSES =
            List.of(TokenStatus.WAITING, TokenStatus.SERVING);

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final TokenRepository tokenRepository;

    public TokenService(
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            TokenRepository tokenRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public TokenResponse createToken(TokenRequest request) {
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + request.doctorId()));
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + request.patientId()));
        
        LocalDate today = LocalDate.now();

        if (request.priority() && tokenRepository.existsActivePriorityToken(
                doctor.getId(), today, ACTIVE_PRIORITY_STATUSES)) {
            throw new IllegalArgumentException("Priority token already exists for this doctor today");
        }

        int tokenNumber = tokenRepository.findHighestTokenNumber(doctor.getId(), today) + 1;
        
        Token token = new Token(doctor, patient, today, tokenNumber, request.priority(), LocalDateTime.now());
        token = tokenRepository.save(token);
        
        return toResponse(token);
    }

    @Transactional
    public TokenResponse advanceQueue(Long doctorId) {
        ensureDoctorExists(doctorId);
        LocalDate today = LocalDate.now();

        Optional<Token> servingToken = tokenRepository.findFirstByDoctor_IdAndTokenDateAndStatus(
                doctorId, today, TokenStatus.SERVING);
                
        servingToken.ifPresent(token -> {
            token.setStatus(TokenStatus.COMPLETED);
            token.setServedAt(LocalDateTime.now());
            tokenRepository.save(token);
        });

        Optional<Token> nextToken = tokenRepository
                .findFirstByDoctor_IdAndTokenDateAndStatusOrderByPriorityDescTokenNumberAsc(
                        doctorId, today, TokenStatus.WAITING);
                        
        if (nextToken.isPresent()) {
            Token token = nextToken.get();
            token.setStatus(TokenStatus.SERVING);
            tokenRepository.save(token);
            return toResponse(token);
        }

        throw new IllegalArgumentException("No waiting tokens available for this doctor today.");
    }

    @Transactional(readOnly = true)
    public WaitTimeResponse getWaitEstimate(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new EntityNotFoundException("Token not found with id: " + tokenId));
                
        if (token.getStatus() == TokenStatus.COMPLETED) {
            throw new IllegalArgumentException("Token has already been served");
        }
        
        if (token.getStatus() == TokenStatus.CANCELLED) {
            throw new IllegalArgumentException("Token is cancelled");
        }

        int tokensAhead = 0;
        int waitTime = 0;
        
        if (token.getStatus() == TokenStatus.WAITING) {
            boolean serving = tokenRepository.findFirstByDoctor_IdAndTokenDateAndStatus(
                    token.getDoctor().getId(), token.getTokenDate(), TokenStatus.SERVING).isPresent();
                    
            tokensAhead = serving ? 1 : 0;
            
            List<Token> waitingTokens = tokenRepository.findByDoctor_IdAndTokenDateAndStatusOrderByPriorityDescTokenNumberAsc(
                    token.getDoctor().getId(), token.getTokenDate(), TokenStatus.WAITING);
                    
            for (Token waitingToken : waitingTokens) {
                if (waitingToken.getId().equals(token.getId())) {
                    break;
                }
                tokensAhead++;
            }
            
            waitTime = tokensAhead * token.getDoctor().getAverageConsultationTime();
        }

        return new WaitTimeResponse(
                token.getTokenNumber(),
                tokensAhead,
                token.getDoctor().getAverageConsultationTime(),
                waitTime
        );
    }

    @Transactional(readOnly = true)
    public List<TokenResponse> getDailyHistory(Long doctorId, LocalDate date) {
        ensureDoctorExists(doctorId);
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<Token> tokens = tokenRepository.findByDoctor_IdAndTokenDateOrderByTokenNumberAsc(doctorId, targetDate);
        
        return tokens.stream()
                .map(this::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public TokenResponse getTokenById(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new EntityNotFoundException("Token not found with id: " + tokenId));
        return toResponse(token);
    }
    
    @Transactional(readOnly = true)
    public List<TokenResponse> getQueue(Long doctorId) {
        ensureDoctorExists(doctorId);
        LocalDate today = LocalDate.now();
        
        List<TokenResponse> queue = new ArrayList<>();
        
        tokenRepository.findFirstByDoctor_IdAndTokenDateAndStatus(
                doctorId, today, TokenStatus.SERVING)
                .ifPresent(token -> queue.add(toResponse(token)));
                
        List<Token> waitingTokens = tokenRepository.findByDoctor_IdAndTokenDateAndStatusOrderByPriorityDescTokenNumberAsc(
                doctorId, today, TokenStatus.WAITING);
                
        waitingTokens.stream().map(this::toResponse).forEach(queue::add);
        
        return queue;
    }

    private void ensureDoctorExists(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new EntityNotFoundException("Doctor not found with id: " + doctorId);
        }
    }

    private TokenResponse toResponse(Token token) {
        return new TokenResponse(
                token.getId(),
                token.getDoctor().getId(),
                token.getPatient().getId(),
                token.getTokenDate(),
                token.getTokenNumber(),
                token.isPriority(),
                token.getStatus(),
                token.getGeneratedAt(),
                token.getServedAt()
        );
    }
}