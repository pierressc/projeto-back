package br.com.uniesp.centralencomendas.itemmaterialencomenda.dto;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;

public record ChecagemMateriaisResponse(
        Long encomendaId,
        String codigoEncomenda,
        boolean materiaisConferidos,
        boolean todosMateriaisComprados,
        long totalMateriais,
        long pendentesCompra
) {
    public static ChecagemMateriaisResponse from(Encomenda encomenda) {
        long total = encomenda.getMateriaisEspeciais().size();
        long pendentes = encomenda.getMateriaisEspeciais().stream()
                .filter(item -> !item.isComprado())
                .count();
        return new ChecagemMateriaisResponse(
                encomenda.getId(),
                encomenda.getCodigo(),
                encomenda.isMateriaisConferidos(),
                encomenda.todosMateriaisComprados(),
                total,
                pendentes
        );
    }
}
