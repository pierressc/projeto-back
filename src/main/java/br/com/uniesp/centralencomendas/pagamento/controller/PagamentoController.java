package br.com.uniesp.centralencomendas.pagamento.controller;

import br.com.uniesp.centralencomendas.pagamento.dto.EstornarPagamentoRequest;
import br.com.uniesp.centralencomendas.pagamento.dto.PagamentoResponse;
import br.com.uniesp.centralencomendas.pagamento.dto.RegistrarPagamentoRequest;
import br.com.uniesp.centralencomendas.pagamento.dto.ResumoFinanceiroResponse;
import br.com.uniesp.centralencomendas.pagamento.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/encomendas/{encomendaId}/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<PagamentoResponse> registrar(
            @PathVariable Long encomendaId,
            @Valid @RequestBody RegistrarPagamentoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamentoService.registrar(encomendaId, request));
    }

    @PatchMapping("/{pagamentoId}/estornar")
    public PagamentoResponse estornar(
            @PathVariable Long encomendaId,
            @PathVariable Long pagamentoId,
            @Valid @RequestBody EstornarPagamentoRequest request
    ) {
        return pagamentoService.estornar(encomendaId, pagamentoId, request);
    }

    @GetMapping
    public List<PagamentoResponse> listar(@PathVariable Long encomendaId) {
        return pagamentoService.listar(encomendaId);
    }

    @GetMapping("/resumo")
    public ResumoFinanceiroResponse consultarResumoFinanceiro(@PathVariable Long encomendaId) {
        return pagamentoService.consultarResumoFinanceiro(encomendaId);
    }
}
