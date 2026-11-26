package br.com.uniesp.centralencomendas.cliente.dto;

import br.com.uniesp.centralencomendas.cliente.entity.Cliente;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nome,
        String telefone,
        String email,
        String observacoes,
        LocalDateTime dataCadastro
) {
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getObservacoes(),
                cliente.getDataCadastro()
        );
    }
}
