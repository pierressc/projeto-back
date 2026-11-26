package br.com.uniesp.centralencomendas.produtocatalogo.controller;

import br.com.uniesp.centralencomendas.produtocatalogo.dto.ProdutoCatalogoAtualizacaoRequest;
import br.com.uniesp.centralencomendas.produtocatalogo.dto.ProdutoCatalogoPrecoRequest;
import br.com.uniesp.centralencomendas.produtocatalogo.dto.ProdutoCatalogoRequest;
import br.com.uniesp.centralencomendas.produtocatalogo.dto.ProdutoCatalogoResponse;
import br.com.uniesp.centralencomendas.produtocatalogo.service.ProdutoCatalogoService;
import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/produtos")
public class ProdutoCatalogoController {

    private final ProdutoCatalogoService produtoCatalogoService;

    public ProdutoCatalogoController(ProdutoCatalogoService produtoCatalogoService) {
        this.produtoCatalogoService = produtoCatalogoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoCatalogoResponse> criar(@Valid @RequestBody ProdutoCatalogoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoCatalogoService.criar(request));
    }

    @GetMapping
    public List<ProdutoCatalogoResponse> listar(
            @RequestParam(required = false) CategoriaProduto categoria,
            @RequestParam(required = false) Boolean apenasAtivos
    ) {
        return produtoCatalogoService.listar(categoria, apenasAtivos);
    }

    @GetMapping("/{id}")
    public ProdutoCatalogoResponse buscarPorId(@PathVariable Long id) {
        return produtoCatalogoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ProdutoCatalogoResponse atualizarCatalogo(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoCatalogoAtualizacaoRequest request
    ) {
        return produtoCatalogoService.atualizarCatalogo(id, request);
    }

    @PatchMapping("/{id}/preco")
    public ProdutoCatalogoResponse reajustarPreco(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoCatalogoPrecoRequest request
    ) {
        return produtoCatalogoService.reajustarPreco(id, request);
    }

    @PostMapping("/{id}/materiais/{materialId}")
    public ProdutoCatalogoResponse associarMaterial(@PathVariable Long id, @PathVariable Long materialId) {
        return produtoCatalogoService.associarMaterial(id, materialId);
    }

    @DeleteMapping("/{id}/materiais/{materialId}")
    public ProdutoCatalogoResponse desvincularMaterial(@PathVariable Long id, @PathVariable Long materialId) {
        return produtoCatalogoService.desvincularMaterial(id, materialId);
    }

    @PatchMapping("/{id}/ativar")
    public ProdutoCatalogoResponse ativar(@PathVariable Long id) {
        return produtoCatalogoService.ativar(id);
    }

    @PatchMapping("/{id}/desativar")
    public ProdutoCatalogoResponse desativar(@PathVariable Long id) {
        return produtoCatalogoService.desativar(id);
    }
}
