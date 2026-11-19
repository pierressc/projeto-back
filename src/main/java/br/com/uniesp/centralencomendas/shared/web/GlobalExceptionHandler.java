package br.com.uniesp.centralencomendas.shared.web;

import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tratador global de excecoes para padronizar respostas HTTP da API REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> handleNotFound(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso nao encontrado", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> handleBusinessRule(RegraNegocioException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negocio violada", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Dados invalidos", "Verifique os campos informados.", request, detalhes);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ErroResposta> handleBadRequest(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Requisicao invalida", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> handleConflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(
                HttpStatus.CONFLICT,
                "Conflito de dados",
                "Ja existe um registro com os mesmos identificadores unicos ou ha vinculos ativos.",
                request,
                List.of()
        );
    }

    private ResponseEntity<ErroResposta> build(
            HttpStatus status,
            String erro,
            String mensagem,
            HttpServletRequest request,
            List<String> detalhes
    ) {
        ErroResposta body = new ErroResposta(
                LocalDateTime.now(),
                status.value(),
                erro,
                mensagem,
                request.getRequestURI(),
                detalhes
        );
        return ResponseEntity.status(status).body(body);
    }
}
