package com.example.QueueManagement.repository;

import com.example.QueueManagement.entity.Token;
import com.example.QueueManagement.entity.TokenStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("select coalesce(max(t.tokenNumber), 0) from Token t where t.doctor.id = :doctorId and t.tokenDate = :tokenDate")
    Integer findHighestTokenNumber(@Param("doctorId") Long doctorId, @Param("tokenDate") LocalDate tokenDate);

    @Query("select count(t) > 0 from Token t where t.doctor.id = :doctorId and t.tokenDate = :tokenDate and t.priority = true and t.status in :statuses")
    boolean existsActivePriorityToken(@Param("doctorId") Long doctorId, @Param("tokenDate") LocalDate tokenDate, @Param("statuses") Collection<TokenStatus> statuses);

    Optional<Token> findFirstByDoctor_IdAndTokenDateAndStatus(Long doctorId, LocalDate tokenDate, TokenStatus status);

    Optional<Token> findFirstByDoctor_IdAndTokenDateAndStatusOrderByPriorityDescTokenNumberAsc(Long doctorId, LocalDate tokenDate, TokenStatus status);

    List<Token> findByDoctor_IdAndTokenDateOrderByTokenNumberAsc(Long doctorId, LocalDate tokenDate);

    List<Token> findByDoctor_IdAndTokenDateAndStatusOrderByPriorityDescTokenNumberAsc(Long doctorId, LocalDate tokenDate, TokenStatus status);
    
    List<Token> findByDoctor_IdAndTokenDateAndStatusInOrderByPriorityDescTokenNumberAsc(Long doctorId, LocalDate tokenDate, Collection<TokenStatus> statuses);
}