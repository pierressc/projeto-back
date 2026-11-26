package br.com.uniesp.centralencomendas.encomenda.dto;

public record ResumoWhatsAppResponse(
        Long encomendaId,
        String codigo,
        String resumoTexto
) {
}
