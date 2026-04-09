package com.ga.JNews.repositories;

import com.ga.JNews.models.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Subscriber findByEmail(String email);
    boolean existsByEmail(String email);
    @Async("executor")
    CompletableFuture<ArrayList<Subscriber>> findAllBy(); // Find all but multithreaded
}
