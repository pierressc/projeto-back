package br.com.uniesp.centralencomendas.entrega.controller;

import br.com.uniesp.centralencomendas.entrega.dto.AlterarModalidadeEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.dto.ConcluirEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.dto.EntregaResponse;
import br.com.uniesp.centralencomendas.entrega.dto.ReagendarEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.service.EntregaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/encomendas/{encomendaId}/entrega")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping
    public EntregaResponse consultar(@PathVariable Long encomendaId) {
        return entregaService.consultarPorEncomenda(encomendaId);
    }

    @PatchMapping("/reagendar")
    public EntregaResponse reagendar(
            @PathVariable Long encomendaId,
            @Valid @RequestBody ReagendarEntregaRequest request
    ) {
        return entregaService.reagendar(encomendaId, request);
    }

    @PatchMapping("/modalidade")
    public EntregaResponse alterarModalidade(
            @PathVariable Long encomendaId,
            @Valid @RequestBody AlterarModalidadeEntregaRequest request
    ) {
        return entregaService.alterarModalidade(encomendaId, request);
    }

    @PatchMapping("/concluir")
    public EntregaResponse concluir(
            @PathVariable Long encomendaId,
            @Valid @RequestBody ConcluirEntregaRequest request
    ) {
        return entregaService.concluir(encomendaId, request);
    }
}
