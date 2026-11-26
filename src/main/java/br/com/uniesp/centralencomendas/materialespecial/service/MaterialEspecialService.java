package br.com.uniesp.centralencomendas.materialespecial.service;

import br.com.uniesp.centralencomendas.materialespecial.dto.MaterialEspecialRequest;
import br.com.uniesp.centralencomendas.materialespecial.dto.MaterialEspecialResponse;
import br.com.uniesp.centralencomendas.materialespecial.entity.MaterialEspecial;
import br.com.uniesp.centralencomendas.materialespecial.repository.MaterialEspecialRepository;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialEspecialService {

    private final MaterialEspecialRepository materialEspecialRepository;

    public MaterialEspecialService(MaterialEspecialRepository materialEspecialRepository) {
        this.materialEspecialRepository = materialEspecialRepository;
    }

    @Transactional
    public MaterialEspecialResponse criar(MaterialEspecialRequest request) {
        if (materialEspecialRepository.findByNomeIgnoreCase(request.nome().trim()).isPresent()) {
            throw new RegraNegocioException("Ja existe material especial cadastrado com esse nome.");
        }
        MaterialEspecial material = new MaterialEspecial(
                request.nome(),
                request.descricao(),
                request.unidadeMedida(),
                request.fornecedorSugerido()
        );
        return MaterialEspecialResponse.from(materialEspecialRepository.save(material));
    }

    @Transactional(readOnly = true)
    public List<MaterialEspecialResponse> listar(Boolean apenasAtivos) {
        List<MaterialEspecial> materiais = Boolean.TRUE.equals(apenasAtivos)
                ? materialEspecialRepository.findByAtivoTrue()
                : materialEspecialRepository.findAll();
        return materiais.stream().map(MaterialEspecialResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MaterialEspecialResponse buscarPorId(Long id) {
        return MaterialEspecialResponse.from(buscarEntidade(id));
    }

    @Transactional
    public MaterialEspecialResponse atualizar(Long id, MaterialEspecialRequest request) {
        MaterialEspecial material = buscarEntidade(id);
        material.atualizarDados(request.nome(), request.descricao(), request.unidadeMedida(), request.fornecedorSugerido());
        return MaterialEspecialResponse.from(materialEspecialRepository.save(material));
    }

    @Transactional
    public MaterialEspecialResponse ativar(Long id) {
        MaterialEspecial material = buscarEntidade(id);
        material.ativar();
        return MaterialEspecialResponse.from(materialEspecialRepository.save(material));
    }

    @Transactional
    public MaterialEspecialResponse desativar(Long id) {
        MaterialEspecial material = buscarEntidade(id);
        material.desativar();
        return MaterialEspecialResponse.from(materialEspecialRepository.save(material));
    }

    private MaterialEspecial buscarEntidade(Long id) {
        return materialEspecialRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material especial nao encontrado com id " + id + "."));
    }
}
