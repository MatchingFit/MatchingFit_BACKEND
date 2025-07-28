package com.example.matching_fit.domain.manager.resume_matching_result.repository;

import com.example.matching_fit.domain.manager.resume_matching_result.entity.ResumeMatchingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ResumeMatchingResultRepository extends JpaRepository<ResumeMatchingResult, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO resume_matching_results (user_id, manager_id)
        VALUES (:userId, :managerId)
        """, nativeQuery = true)
    void insertMatchingResult(@Param("userId") Long userId,
                              @Param("managerId") Long managerId);
}
