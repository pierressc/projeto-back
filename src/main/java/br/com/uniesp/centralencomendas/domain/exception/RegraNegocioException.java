package br.com.uniesp.centralencomendas.domain.exception;

/**
 * Excecao de dominio lancada quando uma operacao viola regras ou travas de seguranca do negocio.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }

    public RegraNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
