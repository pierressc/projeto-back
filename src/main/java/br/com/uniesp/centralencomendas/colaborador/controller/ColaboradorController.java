package br.com.uniesp.centralencomendas.colaborador.controller;

import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorDadosRequest;
import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorRequest;
import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorResponse;
import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorSetorRequest;
import br.com.uniesp.centralencomendas.colaborador.service.ColaboradorService;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @PostMapping
    public ResponseEntity<ColaboradorResponse> criar(@Valid @RequestBody ColaboradorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(colaboradorService.criar(request));
    }

    @GetMapping
    public List<ColaboradorResponse> listar(
            @RequestParam(required = false) SetorColaborador setor,
            @RequestParam(required = false) Boolean apenasAtivos
    ) {
        return colaboradorService.listar(setor, apenasAtivos);
    }

    @GetMapping("/{id}")
    public ColaboradorResponse buscarPorId(@PathVariable Long id) {
        return colaboradorService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ColaboradorResponse atualizarDados(
            @PathVariable Long id,
            @Valid @RequestBody ColaboradorDadosRequest request
    ) {
        return colaboradorService.atualizarDados(id, request);
    }

    @PatchMapping("/{id}/setor")
    public ColaboradorResponse alterarSetor(
            @PathVariable Long id,
            @Valid @RequestBody ColaboradorSetorRequest request
    ) {
        return colaboradorService.alterarSetor(id, request);
    }

    @PatchMapping("/{id}/ativar")
    public ColaboradorResponse ativar(@PathVariable Long id) {
        return colaboradorService.ativar(id);
    }

    @PatchMapping("/{id}/desativar")
    public ColaboradorResponse desativar(@PathVariable Long id) {
        return colaboradorService.desativar(id);
    }
}
