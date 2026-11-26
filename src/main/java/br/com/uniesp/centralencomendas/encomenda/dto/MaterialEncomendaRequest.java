package br.com.uniesp.centralencomendas.encomenda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaterialEncomendaRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 60) String quantidade,
        @Size(max = 300) String observacao
) {
}
