package br.com.uniesp.centralencomendas.agendasemana.controller;

import br.com.uniesp.centralencomendas.agendasemana.dto.AgendaSemanaRequest;
import br.com.uniesp.centralencomendas.agendasemana.dto.AgendaSemanaResponse;
import br.com.uniesp.centralencomendas.agendasemana.dto.AjustarCapacidadeRequest;
import br.com.uniesp.centralencomendas.agendasemana.service.AgendaSemanaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agendas-semana")
public class AgendaSemanaController {

    private final AgendaSemanaService agendaSemanaService;

    public AgendaSemanaController(AgendaSemanaService agendaSemanaService) {
        this.agendaSemanaService = agendaSemanaService;
    }

    @PostMapping
    public ResponseEntity<AgendaSemanaResponse> criar(@Valid @RequestBody AgendaSemanaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendaSemanaService.criar(request));
    }

    @GetMapping
    public List<AgendaSemanaResponse> listar() {
        return agendaSemanaService.listar();
    }

    @GetMapping("/{id}")
    public AgendaSemanaResponse buscarPorId(@PathVariable Long id) {
        return agendaSemanaService.buscarPorId(id);
    }

    @GetMapping("/por-data")
    public AgendaSemanaResponse consultarPorData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        return agendaSemanaService.consultarPorData(data);
    }

    @PatchMapping("/{id}/capacidade")
    public AgendaSemanaResponse ajustarCapacidadeMaxima(
            @PathVariable Long id,
            @Valid @RequestBody AjustarCapacidadeRequest request
    ) {
        return agendaSemanaService.ajustarCapacidadeMaxima(id, request);
    }
}
