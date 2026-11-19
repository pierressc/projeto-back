package br.com.uniesp.centralencomendas.entrega.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReagendarEntregaRequest(
        @NotNull LocalDate novaDataPrevista
) {
}
