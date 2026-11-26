package br.com.uniesp.centralencomendas.agendasemana.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AgendaSemanaRequest(
        @NotBlank @Size(max = 30) String codigoSemana,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim,
        @Min(1) int capacidadeMaximaPedidos
) {
}
