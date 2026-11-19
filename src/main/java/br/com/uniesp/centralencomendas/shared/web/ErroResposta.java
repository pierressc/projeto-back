package br.com.uniesp.centralencomendas.shared.web;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estrutura padronizada de resposta para erros da API REST.
 */
public record ErroResposta(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<String> detalhes
) {
}
