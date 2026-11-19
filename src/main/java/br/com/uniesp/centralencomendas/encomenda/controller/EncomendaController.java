package br.com.uniesp.centralencomendas.encomenda.controller;

import br.com.uniesp.centralencomendas.encomenda.dto.CancelarEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.dto.CriarEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaCadastroResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.ResumoWhatsAppResponse;
import br.com.uniesp.centralencomendas.encomenda.service.EncomendaService;
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

@RestController
@RequestMapping("/api/encomendas")
public class EncomendaController {

    private final EncomendaService encomendaService;

    public EncomendaController(EncomendaService encomendaService) {
        this.encomendaService = encomendaService;
    }

    @PostMapping
    public ResponseEntity<EncomendaCadastroResponse> criar(@Valid @RequestBody CriarEncomendaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(encomendaService.criar(request));
    }

    @PatchMapping("/{id}/liberar-producao")
    public EncomendaCadastroResponse liberarParaProducao(@PathVariable Long id) {
        return encomendaService.liberarParaProducao(id);
    }

    @PatchMapping("/{id}/cancelar")
    public EncomendaCadastroResponse cancelar(
            @PathVariable Long id,
            @Valid @RequestBody CancelarEncomendaRequest request
    ) {
        return encomendaService.cancelar(id, request);
    }

    @GetMapping("/{id}/resumo-whatsapp")
    public ResumoWhatsAppResponse gerarResumoWhatsApp(@PathVariable Long id) {
        return encomendaService.gerarResumoWhatsApp(id);
    }
}
