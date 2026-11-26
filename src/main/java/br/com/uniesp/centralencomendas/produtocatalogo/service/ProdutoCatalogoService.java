package br.com.uniesp.centralencomendas.produtocatalogo.service;

import br.com.uniesp.centralencomendas.materialespecial.entity.MaterialEspecial;
import br.com.uniesp.centralencomendas.materialespecial.repository.MaterialEspecialRepository;
import br.com.uniesp.centralencomendas.produtocatalogo.dto.ProdutoCatalogoAtualizacaoRequest;
import br.com.uniesp.centralencomendas.produtocatalogo.dto.ProdutoCatalogoPrecoRequest;
import br.com.uniesp.centralencomendas.produtocatalogo.dto.ProdutoCatalogoRequest;
import br.com.uniesp.centralencomendas.produtocatalogo.dto.ProdutoCatalogoResponse;
import br.com.uniesp.centralencomendas.produtocatalogo.entity.ProdutoCatalogo;
import br.com.uniesp.centralencomendas.produtocatalogo.repository.ProdutoCatalogoRepository;
import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoCatalogoService {

    private final ProdutoCatalogoRepository produtoCatalogoRepository;
    private final MaterialEspecialRepository materialEspecialRepository;

    public ProdutoCatalogoService(
            ProdutoCatalogoRepository produtoCatalogoRepository,
            MaterialEspecialRepository materialEspecialRepository
    ) {
        this.produtoCatalogoRepository = produtoCatalogoRepository;
        this.materialEspecialRepository = materialEspecialRepository;
    }

    @Transactional
    public ProdutoCatalogoResponse criar(ProdutoCatalogoRequest request) {
        ProdutoCatalogo produto = new ProdutoCatalogo(
                request.nome(),
                request.descricao(),
                request.categoria(),
                request.precoBase(),
                request.pesoCapacidade()
        );
        if (request.materiaisEspeciaisIds() != null) {
            for (Long materialId : request.materiaisEspeciaisIds()) {
                produto.associarMaterialEspecial(buscarMaterial(materialId));
            }
        }
        return ProdutoCatalogoResponse.from(produtoCatalogoRepository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoCatalogoResponse> listar(CategoriaProduto categoria, Boolean apenasAtivos) {
        List<ProdutoCatalogo> lista;
        if (categoria != null) {
            lista = produtoCatalogoRepository.findByCategoria(categoria);
        } else if (Boolean.TRUE.equals(apenasAtivos)) {
            lista = produtoCatalogoRepository.findByAtivoTrue();
        } else {
            lista = produtoCatalogoRepository.findAll();
        }
        return lista.stream()
                .filter(p -> !Boolean.TRUE.equals(apenasAtivos) || p.isAtivo())
                .map(ProdutoCatalogoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoCatalogoResponse buscarPorId(Long id) {
        return ProdutoCatalogoResponse.from(buscarProduto(id));
    }

    @Transactional
    public ProdutoCatalogoResponse atualizarCatalogo(Long id, ProdutoCatalogoAtualizacaoRequest request) {
        ProdutoCatalogo produto = buscarProduto(id);
        produto.atualizarCatalogo(request.nome(), request.descricao(), request.categoria());
        return ProdutoCatalogoResponse.from(produtoCatalogoRepository.save(produto));
    }

    @Transactional
    public ProdutoCatalogoResponse reajustarPreco(Long id, ProdutoCatalogoPrecoRequest request) {
        ProdutoCatalogo produto = buscarProduto(id);
        produto.reajustarPrecoBase(request.novoPrecoBase());
        return ProdutoCatalogoResponse.from(produtoCatalogoRepository.save(produto));
    }

    @Transactional
    public ProdutoCatalogoResponse associarMaterial(Long produtoId, Long materialId) {
        ProdutoCatalogo produto = buscarProduto(produtoId);
        produto.associarMaterialEspecial(buscarMaterial(materialId));
        return ProdutoCatalogoResponse.from(produtoCatalogoRepository.save(produto));
    }

    @Transactional
    public ProdutoCatalogoResponse desvincularMaterial(Long produtoId, Long materialId) {
        ProdutoCatalogo produto = buscarProduto(produtoId);
        produto.desvincularMaterialEspecial(buscarMaterial(materialId));
        return ProdutoCatalogoResponse.from(produtoCatalogoRepository.save(produto));
    }

    @Transactional
    public ProdutoCatalogoResponse ativar(Long id) {
        ProdutoCatalogo produto = buscarProduto(id);
        produto.ativar();
        return ProdutoCatalogoResponse.from(produtoCatalogoRepository.save(produto));
    }

    @Transactional
    public ProdutoCatalogoResponse desativar(Long id) {
        ProdutoCatalogo produto = buscarProduto(id);
        produto.desativar();
        return ProdutoCatalogoResponse.from(produtoCatalogoRepository.save(produto));
    }

    private ProdutoCatalogo buscarProduto(Long id) {
        return produtoCatalogoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto de catalogo nao encontrado com id " + id + "."));
    }

    private MaterialEspecial buscarMaterial(Long id) {
        return materialEspecialRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material especial nao encontrado com id " + id + "."));
    }
}
