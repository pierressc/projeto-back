package br.com.uniesp.centralencomendas.itemencomenda.controller;

import br.com.uniesp.centralencomendas.itemencomenda.dto.ItemEncomendaRequest;
import br.com.uniesp.centralencomendas.itemencomenda.dto.ItemEncomendaResponse;
import br.com.uniesp.centralencomendas.itemencomenda.service.ItemEncomendaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/encomendas/{encomendaId}/itens")
public class ItemEncomendaController {

    private final ItemEncomendaService itemEncomendaService;

    public ItemEncomendaController(ItemEncomendaService itemEncomendaService) {
        this.itemEncomendaService = itemEncomendaService;
    }

    @PostMapping
    public ResponseEntity<ItemEncomendaResponse> adicionar(
            @PathVariable Long encomendaId,
            @Valid @RequestBody ItemEncomendaRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemEncomendaService.adicionar(encomendaId, request));
    }

    @GetMapping
    public List<ItemEncomendaResponse> listar(@PathVariable Long encomendaId) {
        return itemEncomendaService.listar(encomendaId);
    }

    @PutMapping("/{itemId}")
    public ItemEncomendaResponse atualizar(
            @PathVariable Long encomendaId,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemEncomendaRequest request
    ) {
        return itemEncomendaService.atualizar(encomendaId, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> remover(@PathVariable Long encomendaId, @PathVariable Long itemId) {
        itemEncomendaService.remover(encomendaId, itemId);
        return ResponseEntity.noContent().build();
    }
}
