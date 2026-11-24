package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import br.unitins.tp2.model.StatusPedido;
import br.unitins.tp2.model.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public final class PedidoDTOs {
    private PedidoDTOs() {}
    public record ItemRequest(@Positive long produtoId, @Min(1) int quantidade) {}
    public record CreateRequest(@NotNull TipoEntrega tipoEntrega, @NotEmpty List<@NotNull @Valid ItemRequest> itens) {}
    public record ItemResponse(Long produtoId, String nomeProduto, String sku, int quantidade, BigDecimal precoUnitario) {}
    public record Response(Long id, OffsetDateTime dataHora, StatusPedido status, TipoEntrega tipoEntrega,
            BigDecimal frete, LocalDate prazoEntrega, String nomeCliente, String cep, String logradouro,
            String numero, String complemento, String bairro, String municipio, String estado,
            BigDecimal total, List<ItemResponse> itens) {}
}
