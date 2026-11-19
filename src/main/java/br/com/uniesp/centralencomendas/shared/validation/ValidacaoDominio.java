package br.com.uniesp.centralencomendas.shared.validation;

import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;

import java.math.BigDecimal;

/**
 * Utilitario compartilhado para validacoes de invariantes nas entidades de dominio.
 */
public final class ValidacaoDominio {

    private ValidacaoDominio() {
    }

    public static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("O campo '" + campo + "' e obrigatorio.");
        }
        return valor.trim();
    }

    public static String textoOpcional(String valor) {
        return valor == null ? null : valor.trim();
    }

    public static <T> T naoNulo(T valor, String campo) {
        if (valor == null) {
            throw new RegraNegocioException("O campo '" + campo + "' nao pode ser nulo.");
        }
        return valor;
    }

    public static BigDecimal valorPositivo(BigDecimal valor, String campo) {
        naoNulo(valor, campo);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O campo '" + campo + "' deve ser maior que zero.");
        }
        return valor;
    }

    public static BigDecimal valorNaoNegativo(BigDecimal valor, String campo) {
        naoNulo(valor, campo);
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("O campo '" + campo + "' nao pode ser negativo.");
        }
        return valor;
    }

    public static int inteiroPositivo(int valor, String campo) {
        if (valor <= 0) {
            throw new RegraNegocioException("O campo '" + campo + "' deve ser maior que zero.");
        }
        return valor;
    }
}
