package br.com.uniesp.centralencomendas.cliente.controller;

import br.com.uniesp.centralencomendas.cliente.dto.ClienteObservacaoRequest;
import br.com.uniesp.centralencomendas.cliente.dto.ClienteRequest;
import br.com.uniesp.centralencomendas.cliente.dto.ClienteResponse;
import br.com.uniesp.centralencomendas.cliente.service.ClienteService;
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
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.criar(request));
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(required = false) String nome) {
        return clienteService.listar(nome);
    }

    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizarContato(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.atualizarContato(id, request);
    }

    @PatchMapping("/{id}/observacoes")
    public ClienteResponse registrarObservacao(
            @PathVariable Long id,
            @Valid @RequestBody ClienteObservacaoRequest request
    ) {
        return clienteService.registrarObservacao(id, request);
    }
}
