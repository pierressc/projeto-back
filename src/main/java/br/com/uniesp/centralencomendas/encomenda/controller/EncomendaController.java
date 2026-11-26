package br.com.uniesp.centralencomendas.encomenda.controller;

import br.com.uniesp.centralencomendas.encomenda.dto.CriarEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.ItemEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.dto.MaterialEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.service.EncomendaService;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/encomendas")
public class EncomendaController {

    private final EncomendaService encomendaService;

    public EncomendaController(EncomendaService encomendaService) {
        this.encomendaService = encomendaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EncomendaResponse criar(@Valid @RequestBody CriarEncomendaRequest request) {
        return encomendaService.criar(request);
    }

    @GetMapping
    public List<EncomendaResponse> listar(@RequestParam(required = false) StatusEncomenda status) {
        return encomendaService.listar(status);
    }

    @GetMapping("/agenda")
    public List<EncomendaResponse> consultarAgenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return encomendaService.consultarAgenda(inicio, fim);
    }

    @GetMapping("/{id}")
    public EncomendaResponse buscarPorId(@PathVariable Long id) {
        return encomendaService.buscarPorId(id);
    }

    @GetMapping("/{id}/resumo-whatsapp")
    public Map<String, String> gerarResumoWhatsApp(@PathVariable Long id) {
        return Map.of("resumo", encomendaService.gerarResumoWhatsApp(id));
    }

    @PostMapping("/{id}/itens")
    @ResponseStatus(HttpStatus.CREATED)
    public EncomendaResponse adicionarItem(@PathVariable Long id, @Valid @RequestBody ItemEncomendaRequest request) {
        return encomendaService.adicionarItem(id, request);
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    public EncomendaResponse removerItem(@PathVariable Long id, @PathVariable Long itemId) {
        return encomendaService.removerItem(id, itemId);
    }

    @PostMapping("/{id}/materiais")
    @ResponseStatus(HttpStatus.CREATED)
    public EncomendaResponse adicionarMaterial(@PathVariable Long id, @Valid @RequestBody MaterialEncomendaRequest request) {
        return encomendaService.adicionarMaterial(id, request);
    }

    @PatchMapping("/{id}/materiais/{materialId}/comprar")
    public EncomendaResponse confirmarCompraMaterial(@PathVariable Long id, @PathVariable Long materialId) {
        return encomendaService.confirmarCompraMaterial(id, materialId);
    }

    @PatchMapping("/{id}/liberar-producao")
    public EncomendaResponse liberarParaProducao(@PathVariable Long id) {
        return encomendaService.liberarParaProducao(id);
    }

    @PatchMapping("/{id}/pronto")
    public EncomendaResponse marcarComoPronto(@PathVariable Long id) {
        return encomendaService.marcarComoPronto(id);
    }

    @PatchMapping("/{id}/entregar")
    public EncomendaResponse concluirEntrega(@PathVariable Long id) {
        return encomendaService.concluirEntrega(id);
    }

    @PatchMapping("/{id}/cancelar")
    public EncomendaResponse cancelar(@PathVariable Long id, @RequestParam String motivo) {
        return encomendaService.cancelar(id, motivo);
    }
}
