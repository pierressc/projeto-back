package br.com.uniesp.centralencomendas.encomenda.dto;

import br.com.uniesp.centralencomendas.entrega.entity.Entrega;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;

import java.time.LocalDate;

public record EntregaCalendarioItemResponse(
        Long entregaId,
        Long encomendaId,
        String codigoEncomenda,
        String clienteNome,
        StatusEncomenda statusEncomenda,
        TipoEntrega tipoEntrega,
        String enderecoEntrega,
        LocalDate dataPrevista,
        boolean concluida,
        String responsavelEntregaNome
) {
    public static EntregaCalendarioItemResponse from(Entrega entrega) {
        return new EntregaCalendarioItemResponse(
                entrega.getId(),
                entrega.getEncomenda().getId(),
                entrega.getEncomenda().getCodigo(),
                entrega.getEncomenda().getCliente().getNome(),
                entrega.getEncomenda().getStatus(),
                entrega.getTipo(),
                entrega.getEnderecoEntrega(),
                entrega.getDataPrevista(),
                entrega.isConcluida(),
                entrega.getResponsavelEntrega() != null ? entrega.getResponsavelEntrega().getNome() : null
        );
    }
}
