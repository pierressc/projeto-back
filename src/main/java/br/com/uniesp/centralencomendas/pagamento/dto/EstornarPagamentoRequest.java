package br.com.uniesp.centralencomendas.pagamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstornarPagamentoRequest(
        @NotBlank @Size(max = 300) String motivo
) {
}
