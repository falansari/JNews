package com.ga.JNews.repositories;

import com.ga.JNews.models.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Subscriber findByEmail(String email);
    boolean existsByEmail(String email);
}
