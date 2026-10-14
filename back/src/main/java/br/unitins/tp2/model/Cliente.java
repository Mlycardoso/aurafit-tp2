package br.unitins.tp2.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Entity
public class Cliente extends DefaultEntity {
    @Column(length=11, unique=true) public String cpf;
    public LocalDate dataNascimento;
    @Column(columnDefinition="boolean default true") public boolean ativo = true;
    @ElementCollection
    @CollectionTable(name="cliente_telefones", joinColumns=@JoinColumn(name="cliente_id"))
    @OrderColumn(name="ordem")
    @Column(name="numero", length=11, nullable=false)
    public List<String> telefones = new ArrayList<>();
    @Column(nullable=false, length=120) public String nome;
    @Column(nullable=false, length=160) public String email;
    @Column(nullable=false, length=11) public String telefone;
    @Column(nullable=false, length=8) public String cep;
    @Column(nullable=false, length=160) public String logradouro;
    @Column(nullable=false, length=100) public String bairro;
    @Column(nullable=false, length=20) public String numero;
    @Column(length=100) public String complemento;
    @ManyToOne(optional=false)
    @JoinColumn(name="id_municipio", nullable=false)
    public Municipio municipio;
}
