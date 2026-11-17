package br.com.uniesp.centralencomendas.domain.entity;

import br.com.uniesp.centralencomendas.domain.exception.RegraNegocioException;

import java.math.BigDecimal;

/**
 * Utilitario interno para validacoes de invariantes nas entidades de dominio.
 */
final class ValidacaoDominio {

    private ValidacaoDominio() {
    }

    static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("O campo '" + campo + "' e obrigatorio.");
        }
        return valor.trim();
    }

    static String textoOpcional(String valor) {
        return valor == null ? null : valor.trim();
    }

    static <T> T naoNulo(T valor, String campo) {
        if (valor == null) {
            throw new RegraNegocioException("O campo '" + campo + "' nao pode ser nulo.");
        }
        return valor;
    }

    static BigDecimal valorPositivo(BigDecimal valor, String campo) {
        naoNulo(valor, campo);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O campo '" + campo + "' deve ser maior que zero.");
        }
        return valor;
    }

    static BigDecimal valorNaoNegativo(BigDecimal valor, String campo) {
        naoNulo(valor, campo);
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O campo '" + campo + "' nao pode ser negativo.");
        }
        return valor;
    }

    static int inteiroPositivo(int valor, String campo) {
        if (valor <= 0) {
            throw new RegraNegocioException("O campo '" + campo + "' deve ser maior que zero.");
        }
        return valor;
    }
}
