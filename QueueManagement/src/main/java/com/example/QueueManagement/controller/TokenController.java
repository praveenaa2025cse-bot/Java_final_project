package com.example.QueueManagement.controller;

import com.example.QueueManagement.dto.TokenRequest;
import com.example.QueueManagement.dto.TokenResponse;
import com.example.QueueManagement.dto.WaitTimeResponse;
import com.example.QueueManagement.service.TokenService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity<TokenResponse> createToken(@Valid @RequestBody TokenRequest request) {
        TokenResponse token = tokenService.createToken(request);
        URI location = URI.create("/api/tokens/" + token.id());
        return ResponseEntity.created(location).body(token);
    }

    @PutMapping("/serve/{doctorId}")
    public ResponseEntity<TokenResponse> advanceQueue(@PathVariable Long doctorId) {
        return ResponseEntity.ok(tokenService.advanceQueue(doctorId));
    }

    @GetMapping("/{tokenId}/wait-time")
    public ResponseEntity<WaitTimeResponse> getWaitEstimate(@PathVariable Long tokenId) {
        return ResponseEntity.ok(tokenService.getWaitEstimate(tokenId));
    }

    @GetMapping("/history/{doctorId}")
    public ResponseEntity<List<TokenResponse>> getDailyHistory(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(tokenService.getDailyHistory(doctorId, date));
    }

    @GetMapping("/{tokenId}")
    public ResponseEntity<TokenResponse> getTokenById(@PathVariable Long tokenId) {
        return ResponseEntity.ok(tokenService.getTokenById(tokenId));
    }

    @GetMapping("/queue/{doctorId}")
    public ResponseEntity<List<TokenResponse>> getQueue(@PathVariable Long doctorId) {
        return ResponseEntity.ok(tokenService.getQueue(doctorId));
    }
}