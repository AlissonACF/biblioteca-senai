package com.example.demo.entity;

import java.time.LocalDateTime;
import com.example.demo.enums.NivelAcesso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
@EqualsAndHashCode(callSuper = false)
public class Usuario extends BaseEntity {

    @Column(name = "nome")
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "nivel_acesso", nullable = false)
    private NivelAcesso nivelAcesso = NivelAcesso.PADRAO;

    @Column(name = "cpf", unique = true)
    private String cpf;

    private String codigoRecuperacao;

    private LocalDateTime codigoRecuperacaoExpiracao;

    @PrePersist
    @PreUpdate
    public void ensureNome() {
        if (nome == null || nome.isBlank()) {
            nome = email != null ? email : "Usuario";
        }
    }
}
