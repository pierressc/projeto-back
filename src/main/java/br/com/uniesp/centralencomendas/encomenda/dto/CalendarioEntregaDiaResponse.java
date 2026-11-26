package br.com.uniesp.centralencomendas.encomenda.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarioEntregaDiaResponse(
        LocalDate data,
        int totalEntregas,
        List<EntregaCalendarioItemResponse> entregas
) {
}
