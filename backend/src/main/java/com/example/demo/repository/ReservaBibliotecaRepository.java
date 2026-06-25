package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ReservaBiblioteca;

@Repository
public interface ReservaBibliotecaRepository extends BaseRepository<ReservaBiblioteca, Long> {

    @Query("""
        SELECT r FROM ReservaBiblioteca r
        WHERE r.ativo = TRUE
        AND r.recurso.id = :resourceId
        AND r.date = :date
        AND r.startHour = :startHour
        AND r.status IN (
            com.example.demo.enums.ReservationStatus.pending_approval,
            com.example.demo.enums.ReservationStatus.approved,
            com.example.demo.enums.ReservationStatus.checked_in
        )
    """)
    List<ReservaBiblioteca> findActiveConflicts(Long resourceId, LocalDate date, Integer startHour);
}
