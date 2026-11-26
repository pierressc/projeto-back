package br.com.uniesp.centralencomendas.colaborador.dto;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;

public record ColaboradorResponse(
        Long id,
        String nome,
        String email,
        SetorColaborador setor,
        boolean ativo
) {
    public static ColaboradorResponse from(Colaborador colaborador) {
        return new ColaboradorResponse(
                colaborador.getId(),
                colaborador.getNome(),
                colaborador.getEmail(),
                colaborador.getSetor(),
                colaborador.isAtivo()
        );
    }
}
