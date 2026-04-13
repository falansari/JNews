package com.ga.JNews.repositories;

import com.ga.JNews.models.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {
    Newsletter findNewsletterById(long id);
    Newsletter findByNewsletterTitle(String title);
    boolean existsByNewsletterTitle(String title);
}
