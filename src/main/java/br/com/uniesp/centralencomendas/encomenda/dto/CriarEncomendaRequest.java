package br.com.uniesp.centralencomendas.encomenda.dto;

import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarEncomendaRequest(
        @NotBlank @Size(max = 30) String codigo,
        @NotNull Long clienteId,
        @NotNull Long atendenteId,
        @NotNull Long agendaSemanaId,
        @NotNull CanalOrigem canalOrigem,
        @NotNull LocalDate dataPrazoPrometido,
        @NotNull @DecimalMin("0.00") BigDecimal valorSinalExigido,
        @Min(0) int prazoAlteracaoSemCustoDias,
        @NotNull TipoEntrega tipoEntrega,
        @Size(max = 300) String enderecoEntrega,
        @Size(max = 1000) String observacoesGerais
) {
}
