package br.com.uniesp.centralencomendas.shared.enums;

/**
 * Ciclo de vida operacional de uma encomenda no painel de andamento.
 */
public enum StatusEncomenda {
    AGUARDANDO_SINAL_MATERIAL,
    EM_PRODUCAO,
    PRONTO,
    ENTREGUE,
    CANCELADO
}
