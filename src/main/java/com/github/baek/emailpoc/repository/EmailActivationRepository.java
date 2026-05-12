package com.github.baek.emailpoc.repository;

import com.github.baek.emailpoc.domain.EmailActivation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailActivationRepository extends JpaRepository<EmailActivation,Long> {
    Optional<EmailActivation> findByToken(String token);

    List<EmailActivation> findAllByEmailAndActivatedFalseAndExpiredFalse(String email);

}
