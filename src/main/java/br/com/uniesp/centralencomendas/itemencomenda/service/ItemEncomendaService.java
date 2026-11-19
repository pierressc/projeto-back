package br.com.uniesp.centralencomendas.itemencomenda.service;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.itemencomenda.dto.ItemEncomendaRequest;
import br.com.uniesp.centralencomendas.itemencomenda.dto.ItemEncomendaResponse;
import br.com.uniesp.centralencomendas.itemencomenda.entity.ItemEncomenda;
import br.com.uniesp.centralencomendas.itemencomenda.repository.ItemEncomendaRepository;
import br.com.uniesp.centralencomendas.produtocatalogo.repository.ProdutoCatalogoRepository;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemEncomendaService {

    private final EncomendaRepository encomendaRepo;
    private final ProdutoCatalogoRepository produtoRepo;
    private final ItemEncomendaRepository itemRepo;

    public ItemEncomendaService(
            EncomendaRepository encomendaRepo,
            ProdutoCatalogoRepository produtoRepo,
            ItemEncomendaRepository itemRepo
    ) {
        this.encomendaRepo = encomendaRepo;
        this.produtoRepo = produtoRepo;
        this.itemRepo = itemRepo;
    }

    @Transactional
    public ItemEncomendaResponse adicionar(Long encomendaId, ItemEncomendaRequest req) {
        Encomenda encomenda = findEncomenda(encomendaId);
        var produto = produtoRepo.findById(req.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado: " + req.produtoId()));
        var unitario = req.valorUnitario() != null ? req.valorUnitario() : produto.getPrecoBase();
        ItemEncomenda item = encomenda.adicionarItem(produto, req.quantidade(), unitario, req.personalizacao());
        encomendaRepo.saveAndFlush(encomenda);
        return ItemEncomendaResponse.from(item);
    }

    @Transactional(readOnly = true)
    public List<ItemEncomendaResponse> listar(Long encomendaId) {
        findEncomenda(encomendaId);
        return itemRepo.findByEncomendaId(encomendaId).stream().map(ItemEncomendaResponse::from).toList();
    }

    @Transactional
    public ItemEncomendaResponse atualizar(Long encomendaId, Long itemId, ItemEncomendaRequest req) {
        Encomenda encomenda = findEncomenda(encomendaId);
        ItemEncomenda item = findItem(itemId);
        var unitario = req.valorUnitario() != null ? req.valorUnitario() : item.getValorUnitario();
        encomenda.atualizarItem(item, req.quantidade(), unitario, req.personalizacao());
        encomendaRepo.saveAndFlush(encomenda);
        return ItemEncomendaResponse.from(item);
    }

    @Transactional
    public void remover(Long encomendaId, Long itemId) {
        Encomenda encomenda = findEncomenda(encomendaId);
        encomenda.removerItem(findItem(itemId));
        encomendaRepo.save(encomenda);
    }

    private Encomenda findEncomenda(Long id) {
        return encomendaRepo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada: " + id));
    }

    private ItemEncomenda findItem(Long id) {
        return itemRepo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item nao encontrado: " + id));
    }
}
