package com.ga.JNews.repositories;

import com.ga.JNews.models.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {
    boolean existsById(Long id);
    Newsletter findByTitle(String title);
    boolean existsByTitle(String title);

    /**
     * Asynchronous find all.
     * @return CompletableFuture ArrayList Newsletter
     */
    @Async("executor")
    CompletableFuture<ArrayList<Newsletter>> findAllBy();
}
