package br.com.uniesp.centralencomendas.colaborador.service;

import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorDadosRequest;
import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorRequest;
import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorResponse;
import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorSetorRequest;
import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.repository.ColaboradorRepository;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
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
        if (colaboradorRepository.findByEmailIgnoreCase(request.email().trim()).isPresent()) {
            throw new RegraNegocioException("Ja existe colaborador cadastrado com o e-mail informado.");
        }
        Colaborador colaborador = new Colaborador(request.nome(), request.email(), request.setor());
        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listar(SetorColaborador setor, Boolean apenasAtivos) {
        List<Colaborador> lista;
        if (setor != null) {
            lista = colaboradorRepository.findBySetor(setor);
        } else if (Boolean.TRUE.equals(apenasAtivos)) {
            lista = colaboradorRepository.findByAtivoTrue();
        } else {
            lista = colaboradorRepository.findAll();
        }
        return lista.stream()
                .filter(c -> !Boolean.TRUE.equals(apenasAtivos) || c.isAtivo())
                .map(ColaboradorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ColaboradorResponse buscarPorId(Long id) {
        return ColaboradorResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ColaboradorResponse atualizarDados(Long id, ColaboradorDadosRequest request) {
        Colaborador colaborador = buscarEntidade(id);
        colaborador.atualizarDados(request.nome(), request.email());
        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    @Transactional
    public ColaboradorResponse alterarSetor(Long id, ColaboradorSetorRequest request) {
        Colaborador colaborador = buscarEntidade(id);
        colaborador.alterarSetor(request.setor());
        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    @Transactional
    public ColaboradorResponse ativar(Long id) {
        Colaborador colaborador = buscarEntidade(id);
        colaborador.ativar();
        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    @Transactional
    public ColaboradorResponse desativar(Long id) {
        Colaborador colaborador = buscarEntidade(id);
        colaborador.desativar();
        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    private Colaborador buscarEntidade(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador nao encontrado com id " + id + "."));
    }
}
