package br.com.uniesp.centralencomendas.materialespecial.controller;

import br.com.uniesp.centralencomendas.materialespecial.dto.MaterialEspecialRequest;
import br.com.uniesp.centralencomendas.materialespecial.dto.MaterialEspecialResponse;
import br.com.uniesp.centralencomendas.materialespecial.service.MaterialEspecialService;
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
@RequestMapping("/api/materiais-especiais")
public class MaterialEspecialController {

    private final MaterialEspecialService materialEspecialService;

    public MaterialEspecialController(MaterialEspecialService materialEspecialService) {
        this.materialEspecialService = materialEspecialService;
    }

    @PostMapping
    public ResponseEntity<MaterialEspecialResponse> criar(@Valid @RequestBody MaterialEspecialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialEspecialService.criar(request));
    }

    @GetMapping
    public List<MaterialEspecialResponse> listar(@RequestParam(required = false) Boolean apenasAtivos) {
        return materialEspecialService.listar(apenasAtivos);
    }

    @GetMapping("/{id}")
    public MaterialEspecialResponse buscarPorId(@PathVariable Long id) {
        return materialEspecialService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public MaterialEspecialResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MaterialEspecialRequest request
    ) {
        return materialEspecialService.atualizar(id, request);
    }

    @PatchMapping("/{id}/ativar")
    public MaterialEspecialResponse ativar(@PathVariable Long id) {
        return materialEspecialService.ativar(id);
    }

    @PatchMapping("/{id}/desativar")
    public MaterialEspecialResponse desativar(@PathVariable Long id) {
        return materialEspecialService.desativar(id);
    }
}
