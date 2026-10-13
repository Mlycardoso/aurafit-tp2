package br.unitins.tp2.model;
import jakarta.persistence.*;
@Entity
public class Estado extends DefaultEntity {
    @Column(nullable=false, unique=true, length=2)
    public String sigla;
    @Column(nullable=false, length=80)
    public String nome;
}
