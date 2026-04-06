package com.ga.JNews.repositories;

import com.ga.JNews.models.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findByUserId(Long userId);
    boolean existsByUser_Id(Long userId);
}
