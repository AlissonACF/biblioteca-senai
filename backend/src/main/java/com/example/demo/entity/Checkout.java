package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "checkout")
@EqualsAndHashCode(callSuper = false)
public class Checkout extends BaseEntity {

    @Column(name = "prevencoes", nullable = false)
    private int prevencoes;

    @Column(name = "lesoes", nullable = false)
    private int lesoes;

    @ManyToOne
    private Posto posto;

    @ManyToOne
    private Arquivo foto;

    @Column(name = "guarda_vidas_email")
    private String guardaVidasEmail;
}
