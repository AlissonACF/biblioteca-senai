package com.example.demo.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "checkins")
@EqualsAndHashCode(callSuper = false)
public class Checkin extends BaseEntity {

    @ManyToOne
    private Posto posto;

    @ManyToOne
    private Arquivo foto;

    @Column(name = "guarda_vidas_email")
    private String guardaVidasEmail;

}
