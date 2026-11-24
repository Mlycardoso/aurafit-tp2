package br.unitins.tp2.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ItemPedido extends DefaultEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "pedido_id", nullable = false)
    public Pedido pedido;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "produto_id")
    public Produto produto;
    @Column(nullable = false, length = 80) public String nomeProduto;
    @Column(length = 40) public String sku;
    @Column(nullable = false) public int quantidade;
    @Column(nullable = false, precision = 10, scale = 2) public BigDecimal precoUnitario;
}
