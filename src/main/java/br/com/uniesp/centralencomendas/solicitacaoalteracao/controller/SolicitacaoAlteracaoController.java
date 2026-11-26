package br.com.uniesp.centralencomendas.solicitacaoalteracao.controller;

import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.AvaliarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitacaoAlteracaoResponse;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.service.SolicitacaoAlteracaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/encomendas/{encomendaId}/alteracoes")
public class SolicitacaoAlteracaoController {

    private final SolicitacaoAlteracaoService solicitacaoService;

    public SolicitacaoAlteracaoController(SolicitacaoAlteracaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitacaoAlteracaoResponse solicitar(
            @PathVariable Long encomendaId,
            @Valid @RequestBody SolicitarAlteracaoRequest request
    ) {
        return solicitacaoService.solicitar(encomendaId, request);
    }

    @GetMapping
    public List<SolicitacaoAlteracaoResponse> listar(@PathVariable Long encomendaId) {
        return solicitacaoService.listarPorEncomenda(encomendaId);
    }

    @PatchMapping("/{alteracaoId}/aprovar")
    public SolicitacaoAlteracaoResponse aprovar(
            @PathVariable Long encomendaId,
            @PathVariable Long alteracaoId,
            @Valid @RequestBody AvaliarAlteracaoRequest request
    ) {
        return solicitacaoService.aprovar(encomendaId, alteracaoId, request);
    }

    @PatchMapping("/{alteracaoId}/recusar")
    public SolicitacaoAlteracaoResponse recusar(
            @PathVariable Long encomendaId,
            @PathVariable Long alteracaoId,
            @Valid @RequestBody AvaliarAlteracaoRequest request
    ) {
        return solicitacaoService.recusar(encomendaId, alteracaoId, request);
    }
}
