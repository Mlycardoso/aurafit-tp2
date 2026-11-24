package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Pedido extends DefaultEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "cliente_id", nullable = false)
    public Cliente cliente;
    @Column(nullable = false) public OffsetDateTime dataHora;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) public StatusPedido status;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) public TipoEntrega tipoEntrega;
    @Column(nullable = false, precision = 10, scale = 2) public BigDecimal frete;
    @Column(nullable = false) public LocalDate prazoEntrega;
    @Column(nullable = false, length = 120) public String nomeCliente;
    @Column(nullable = false, length = 8) public String cep;
    @Column(nullable = false, length = 160) public String logradouro;
    @Column(nullable = false, length = 20) public String numero;
    @Column(length = 100) public String complemento;
    @Column(nullable = false, length = 100) public String bairro;
    @Column(nullable = false, length = 100) public String municipio;
    @Column(nullable = false, length = 60) public String estado;
    @Column(nullable = false, precision = 12, scale = 2) public BigDecimal total;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<ItemPedido> itens = new ArrayList<>();
}
