package com.example.demo.entity;

import com.example.demo.enums.ResourceKind;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "biblioteca_recurso")
@EqualsAndHashCode(callSuper = false)
public class RecursoBiblioteca extends BaseEntity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceKind kind;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;
}
