package br.unitins.tp2.model;
import jakarta.persistence.*;
@Entity
public class Municipio extends DefaultEntity {
    @Column(nullable=false, unique=true, length=7)
    public String codigoIbge;
    @Column(nullable=false, length=120)
    public String nome;
    @ManyToOne(optional=false)
    @JoinColumn(name="id_estado", nullable=false)
    public Estado estado;
}
