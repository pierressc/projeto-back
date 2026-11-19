package br.com.uniesp.centralencomendas.agendasemana.dto;

import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;

import java.time.LocalDate;

public record AgendaSemanaResponse(
        Long id,
        String codigoSemana,
        LocalDate dataInicio,
        LocalDate dataFim,
        int capacidadeMaximaPedidos,
        int pedidosReservados,
        int vagasRestantes,
        boolean possuiVagaDisponivel
) {
    public static AgendaSemanaResponse from(AgendaSemana entity) {
        return new AgendaSemanaResponse(
                entity.getId(),
                entity.getCodigoSemana(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.getCapacidadeMaximaPedidos(),
                entity.getPedidosReservados(),
                entity.calcularVagasRestantes(),
                entity.possuiVagaDisponivel()
        );
    }
}
