package br.unitins.tp2.model;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Usuario extends DefaultEntity {
    @Column(nullable = false, length = 120)
    public String nome;
    @Column(nullable = false, unique = true, length = 160)
    public String email;
    @JsonIgnore
    @Column(nullable = false, length = 255)
    public String senhaHash;
    @Column(nullable = false)
    public boolean ativo;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_perfis", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false)
    public Set<Perfil> perfis = new HashSet<>();
}
