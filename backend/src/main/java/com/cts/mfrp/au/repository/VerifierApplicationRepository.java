package com.cts.mfrp.au.repository;

import com.cts.mfrp.au.model.VerifierApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VerifierApplicationRepository extends JpaRepository<VerifierApplication, Long> {
    VerifierApplication findByEmail(String email);
    List<VerifierApplication> findByStatusOrderBySubmittedAtDesc(String status);
    List<VerifierApplication> findAllByOrderBySubmittedAtDesc();
}
