package br.com.uniesp.centralencomendas.colaborador.controller;

import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorRequest;
import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorResponse;
import br.com.uniesp.centralencomendas.colaborador.service.ColaboradorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public ColaboradorResponse criar(@Valid @RequestBody ColaboradorRequest request) {
        return colaboradorService.criar(request);
    }

    @GetMapping
    public List<ColaboradorResponse> listar() {
        return colaboradorService.listar();
    }

    @GetMapping("/{id}")
    public ColaboradorResponse buscarPorId(@PathVariable Long id) {
        return colaboradorService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ColaboradorResponse atualizar(@PathVariable Long id, @Valid @RequestBody ColaboradorRequest request) {
        return colaboradorService.atualizar(id, request);
    }

    @PatchMapping("/{id}/ativar")
    public ColaboradorResponse ativar(@PathVariable Long id) {
        return colaboradorService.alterarStatusAtivo(id, true);
    }

    @PatchMapping("/{id}/desativar")
    public ColaboradorResponse desativar(@PathVariable Long id) {
        return colaboradorService.alterarStatusAtivo(id, false);
    }
}
