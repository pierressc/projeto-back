package br.com.uniesp.centralencomendas.solicitacaoalteracao.controller;

import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.AprovarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.ProducaoProntaResponse;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.RecusarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitacaoAlteracaoResponse;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.service.SolicitacaoAlteracaoService;
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
@RequestMapping("/api/encomendas/{encomendaId}")
public class SolicitacaoAlteracaoController {

    private final SolicitacaoAlteracaoService solicitacaoService;

    public SolicitacaoAlteracaoController(SolicitacaoAlteracaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping("/alteracoes")
    public ResponseEntity<SolicitacaoAlteracaoResponse> solicitar(
            @PathVariable Long encomendaId,
            @Valid @RequestBody SolicitarAlteracaoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoService.solicitar(encomendaId, request));
    }

    @GetMapping("/alteracoes")
    public List<SolicitacaoAlteracaoResponse> listarPorEncomenda(@PathVariable Long encomendaId) {
        return solicitacaoService.listarPorEncomenda(encomendaId);
    }

    @PatchMapping("/alteracoes/{solicitacaoId}/aprovar")
    public SolicitacaoAlteracaoResponse aprovar(
            @PathVariable Long encomendaId,
            @PathVariable Long solicitacaoId,
            @Valid @RequestBody AprovarAlteracaoRequest request
    ) {
        return solicitacaoService.aprovar(encomendaId, solicitacaoId, request);
    }

    @PatchMapping("/alteracoes/{solicitacaoId}/recusar")
    public SolicitacaoAlteracaoResponse recusar(
            @PathVariable Long encomendaId,
            @PathVariable Long solicitacaoId,
            @Valid @RequestBody RecusarAlteracaoRequest request
    ) {
        return solicitacaoService.recusar(encomendaId, solicitacaoId, request);
    }

    @PatchMapping("/marcar-pronto")
    public ProducaoProntaResponse marcarEncomendaComoPronta(@PathVariable Long encomendaId) {
        return solicitacaoService.marcarEncomendaComoPronta(encomendaId);
    }
}
