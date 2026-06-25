package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.enums.ReservationStatus;
import com.example.demo.enums.ResourceKind;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "biblioteca_reserva")
@EqualsAndHashCode(callSuper = false)
public class ReservaBiblioteca extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id", nullable = false)
    private RecursoBiblioteca recurso;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String resourceName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceKind resourceKind;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer startHour;

    @Column(nullable = false)
    private Integer people;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Long approvedBy;
    private LocalDateTime decidedAt;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private LocalDateTime confirmedAt;

    @Column(length = 500)
    private String rejectionReason;
}
