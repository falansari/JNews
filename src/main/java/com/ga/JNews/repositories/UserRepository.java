package com.ga.JNews.repositories;

import com.ga.JNews.models.User;
import com.ga.JNews.models.enums.ROLE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    User findByEmail(String email);
    boolean existsByRole(ROLE role);
}
