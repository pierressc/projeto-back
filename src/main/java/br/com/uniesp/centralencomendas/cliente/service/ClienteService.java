package br.com.uniesp.centralencomendas.cliente.service;

import br.com.uniesp.centralencomendas.cliente.dto.ClienteObservacaoRequest;
import br.com.uniesp.centralencomendas.cliente.dto.ClienteRequest;
import br.com.uniesp.centralencomendas.cliente.dto.ClienteResponse;
import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import br.com.uniesp.centralencomendas.cliente.repository.ClienteRepository;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        Cliente cliente = new Cliente(request.nome(), request.telefone(), request.email(), request.observacoes());
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String nome) {
        List<Cliente> clientes = (nome == null || nome.isBlank())
                ? clienteRepository.findAll()
                : clienteRepository.findByNomeContainingIgnoreCase(nome.trim());
        return clientes.stream().map(ClienteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return ClienteResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ClienteResponse atualizarContato(Long id, ClienteRequest request) {
        Cliente cliente = buscarEntidade(id);
        cliente.atualizarContato(request.nome(), request.telefone(), request.email());
        if (request.observacoes() != null) {
            cliente.registrarObservacao(request.observacoes());
        }
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse registrarObservacao(Long id, ClienteObservacaoRequest request) {
        Cliente cliente = buscarEntidade(id);
        cliente.registrarObservacao(request.observacoes());
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    private Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado com id " + id + "."));
    }
}
