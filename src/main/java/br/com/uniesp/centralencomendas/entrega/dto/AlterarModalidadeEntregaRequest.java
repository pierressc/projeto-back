package br.com.uniesp.centralencomendas.entrega.dto;

import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlterarModalidadeEntregaRequest(
        @NotNull TipoEntrega novoTipo,
        @Size(max = 300) String novoEndereco
) {
}
