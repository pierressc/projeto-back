package br.com.uniesp.centralencomendas.itemmaterialencomenda.dto;

import jakarta.validation.constraints.Size;

public record ItemMaterialObservacaoRequest(
        @Size(max = 300) String observacao
) {
}
