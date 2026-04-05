package com.ga.JNews.repositories;

import com.ga.JNews.models.User;
import com.ga.JNews.models.Verification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Verification findByToken(String token);
    User getUserByToken(String token);
}
