package br.com.uniesp.centralencomendas.encomenda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarEncomendaRequest(
        @NotBlank @Size(max = 300) String motivo
) {
}
