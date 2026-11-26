package br.com.uniesp.centralencomendas.encomenda.controller;

import br.com.uniesp.centralencomendas.encomenda.dto.CalendarioEntregaDiaResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaDetalheResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaResumoResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.PainelAndamentoResponse;
import br.com.uniesp.centralencomendas.encomenda.service.EncomendaConsultaService;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/encomendas")
public class EncomendaConsultaController {

    private final EncomendaConsultaService consultaService;

    public EncomendaConsultaController(EncomendaConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public List<EncomendaResumoResponse> listar(
            @RequestParam(required = false) StatusEncomenda status,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long agendaSemanaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return consultaService.listar(status, clienteId, agendaSemanaId, dataInicio, dataFim);
    }

    @GetMapping("/{id}")
    public EncomendaDetalheResponse detalhar(@PathVariable Long id) {
        return consultaService.detalhar(id);
    }

    @GetMapping("/painel-andamento")
    public PainelAndamentoResponse painelAndamento() {
        return consultaService.painelAndamento();
    }

    @GetMapping("/calendario-entregas")
    public List<CalendarioEntregaDiaResponse> calendarioEntregas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return consultaService.calendarioEntregas(dataInicio, dataFim);
    }
}
