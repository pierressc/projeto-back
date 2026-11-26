package br.com.uniesp.centralencomendas.itemmaterialencomenda.controller;

import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ChecagemMateriaisResponse;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialEncomendaRequest;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialEncomendaResponse;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialObservacaoRequest;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.service.ItemMaterialEncomendaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/encomendas/{encomendaId}/materiais")
public class ItemMaterialEncomendaController {

    private final ItemMaterialEncomendaService itemMaterialService;

    public ItemMaterialEncomendaController(ItemMaterialEncomendaService itemMaterialService) {
        this.itemMaterialService = itemMaterialService;
    }

    @PostMapping
    public ResponseEntity<ItemMaterialEncomendaResponse> adicionar(
            @PathVariable Long encomendaId,
            @Valid @RequestBody ItemMaterialEncomendaRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemMaterialService.adicionar(encomendaId, request));
    }

    @GetMapping
    public List<ItemMaterialEncomendaResponse> listar(
            @PathVariable Long encomendaId,
            @RequestParam(defaultValue = "false") boolean apenasPendentes
    ) {
        return itemMaterialService.listar(encomendaId, apenasPendentes);
    }

    @PatchMapping("/{itemMaterialId}/confirmar-compra")
    public ItemMaterialEncomendaResponse confirmarCompra(
            @PathVariable Long encomendaId,
            @PathVariable Long itemMaterialId
    ) {
        return itemMaterialService.confirmarCompra(encomendaId, itemMaterialId);
    }

    @PatchMapping("/{itemMaterialId}/estornar-confirmacao")
    public ItemMaterialEncomendaResponse estornarConfirmacao(
            @PathVariable Long encomendaId,
            @PathVariable Long itemMaterialId
    ) {
        return itemMaterialService.estornarConfirmacao(encomendaId, itemMaterialId);
    }

    @PatchMapping("/{itemMaterialId}/observacao")
    public ItemMaterialEncomendaResponse atualizarObservacao(
            @PathVariable Long encomendaId,
            @PathVariable Long itemMaterialId,
            @Valid @RequestBody ItemMaterialObservacaoRequest request
    ) {
        return itemMaterialService.atualizarObservacao(encomendaId, itemMaterialId, request);
    }

    @PatchMapping("/concluir-checagem")
    public ChecagemMateriaisResponse concluirChecagem(@PathVariable Long encomendaId) {
        return itemMaterialService.concluirChecagem(encomendaId);
    }
}
