package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.example.demo.enums.PostoStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posto")
@EqualsAndHashCode(callSuper = false)
public class Posto extends BaseEntity {

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descricao", nullable = true)
    private String descricao;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostoStatus status = PostoStatus.DISPONIVEL;

}


