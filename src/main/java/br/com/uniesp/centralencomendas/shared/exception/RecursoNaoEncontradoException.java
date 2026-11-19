package br.com.uniesp.centralencomendas.shared.exception;

/**
 * Excecao lancada quando um recurso solicitado nao e encontrado na base de dados.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
