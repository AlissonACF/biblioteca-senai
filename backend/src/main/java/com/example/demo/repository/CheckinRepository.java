package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.example.demo.entity.Checkin;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long>{


    @Query("""
        SELECT c FROM Checkin c
        WHERE c.ativo = TRUE
        ORDER BY c.createdAt DESC
    """)
    List<Checkin> findAllAtivosOrderByCreatedAtDesc();
}
