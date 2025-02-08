package com.cybercore.companion.repository;

import com.cybercore.companion.model.Coreling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorelingRepository extends JpaRepository<Coreling, Long> {
    Optional<Coreling> findByUserId(Long userId);
}
