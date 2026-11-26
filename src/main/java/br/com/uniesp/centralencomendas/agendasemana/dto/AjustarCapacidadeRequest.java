package br.com.uniesp.centralencomendas.agendasemana.dto;

import jakarta.validation.constraints.Min;

public record AjustarCapacidadeRequest(
        @Min(1) int novaCapacidade
) {
}
