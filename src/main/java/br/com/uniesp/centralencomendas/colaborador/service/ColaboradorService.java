package br.com.uniesp.centralencomendas.colaborador.service;

import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorRequest;
import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorResponse;
import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.repository.ColaboradorRepository;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;

    public ColaboradorService(ColaboradorRepository colaboradorRepository) {
        this.colaboradorRepository = colaboradorRepository;
    }

    @Transactional
    public ColaboradorResponse criar(ColaboradorRequest request) {
        Colaborador colaborador = new Colaborador(request.nome(), request.email(), request.setor());
        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listar() {
        return colaboradorRepository.findAll().stream().map(ColaboradorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ColaboradorResponse buscarPorId(Long id) {
        return ColaboradorResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ColaboradorResponse atualizar(Long id, ColaboradorRequest request) {
        Colaborador colaborador = buscarEntidade(id);
        colaborador.atualizarDados(request.nome(), request.email());
        colaborador.alterarSetor(request.setor());
        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    @Transactional
    public ColaboradorResponse alterarStatusAtivo(Long id, boolean ativo) {
        Colaborador colaborador = buscarEntidade(id);
        if (ativo) {
            colaborador.ativar();
        } else {
            colaborador.desativar();
        }
        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    public Colaborador buscarEntidade(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador nao encontrado: " + id));
    }
}
