package br.com.uniesp.centralencomendas.cliente.dto;

import jakarta.validation.constraints.Size;

public record ClienteObservacaoRequest(
        @Size(max = 500) String observacoes
) {
}
