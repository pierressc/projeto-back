package br.com.uniesp.centralencomendas.itemmaterialencomenda.service;

import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ChecagemMateriaisResponse;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialEncomendaRequest;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialEncomendaResponse;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialObservacaoRequest;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.entity.ItemMaterialEncomenda;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.repository.ItemMaterialEncomendaRepository;
import br.com.uniesp.centralencomendas.materialespecial.entity.MaterialEspecial;
import br.com.uniesp.centralencomendas.materialespecial.repository.MaterialEspecialRepository;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemMaterialEncomendaService {

    private final EncomendaRepository encomendaRepository;
    private final MaterialEspecialRepository materialEspecialRepository;
    private final ItemMaterialEncomendaRepository itemMaterialRepository;

    public ItemMaterialEncomendaService(
            EncomendaRepository encomendaRepository,
            MaterialEspecialRepository materialEspecialRepository,
            ItemMaterialEncomendaRepository itemMaterialRepository
    ) {
        this.encomendaRepository = encomendaRepository;
        this.materialEspecialRepository = materialEspecialRepository;
        this.itemMaterialRepository = itemMaterialRepository;
    }

    @Transactional
    public ItemMaterialEncomendaResponse adicionar(Long encomendaId, ItemMaterialEncomendaRequest request) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        MaterialEspecial material = materialEspecialRepository.findById(request.materialId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material especial nao encontrado com id " + request.materialId() + "."));
        if (!material.isAtivo()) {
            throw new RegraNegocioException("Nao e possivel vincular material especial inativo: " + material.getNome() + ".");
        }
        ItemMaterialEncomenda itemMaterial = encomenda.adicionarMaterialEspecial(
                material,
                request.quantidadeNecessaria(),
                request.observacao()
        );
        encomendaRepository.saveAndFlush(encomenda);
        return ItemMaterialEncomendaResponse.from(itemMaterial);
    }

    @Transactional(readOnly = true)
    public List<ItemMaterialEncomendaResponse> listar(Long encomendaId, boolean apenasPendentes) {
        buscarEncomenda(encomendaId);
        List<ItemMaterialEncomenda> itens = apenasPendentes
                ? itemMaterialRepository.findByEncomendaIdAndCompradoFalse(encomendaId)
                : itemMaterialRepository.findByEncomendaId(encomendaId);
        return itens.stream().map(ItemMaterialEncomendaResponse::from).toList();
    }

    @Transactional
    public ItemMaterialEncomendaResponse confirmarCompra(Long encomendaId, Long itemMaterialId) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        ItemMaterialEncomenda itemMaterial = buscarItemMaterial(itemMaterialId);
        encomenda.confirmarCompraMaterial(itemMaterial);
        encomendaRepository.saveAndFlush(encomenda);
        return ItemMaterialEncomendaResponse.from(itemMaterial);
    }

    @Transactional
    public ItemMaterialEncomendaResponse estornarConfirmacao(Long encomendaId, Long itemMaterialId) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        ItemMaterialEncomenda itemMaterial = buscarItemMaterial(itemMaterialId);
        validarPertencimento(encomenda, itemMaterial);
        itemMaterial.estornarConfirmacao();
        itemMaterialRepository.saveAndFlush(itemMaterial);
        return ItemMaterialEncomendaResponse.from(itemMaterial);
    }

    @Transactional
    public ItemMaterialEncomendaResponse atualizarObservacao(
            Long encomendaId,
            Long itemMaterialId,
            ItemMaterialObservacaoRequest request
    ) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        ItemMaterialEncomenda itemMaterial = buscarItemMaterial(itemMaterialId);
        validarPertencimento(encomenda, itemMaterial);
        itemMaterial.registrarObservacao(request.observacao());
        return ItemMaterialEncomendaResponse.from(itemMaterialRepository.saveAndFlush(itemMaterial));
    }

    @Transactional
    public ChecagemMateriaisResponse concluirChecagem(Long encomendaId) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        encomenda.concluirChecagemMateriais();
        encomendaRepository.saveAndFlush(encomenda);
        return ChecagemMateriaisResponse.from(encomenda);
    }

    private Encomenda buscarEncomenda(Long encomendaId) {
        return encomendaRepository.findById(encomendaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada com id " + encomendaId + "."));
    }

    private ItemMaterialEncomenda buscarItemMaterial(Long itemMaterialId) {
        return itemMaterialRepository.findById(itemMaterialId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de material especial nao encontrado com id " + itemMaterialId + "."));
    }

    private void validarPertencimento(Encomenda encomenda, ItemMaterialEncomenda itemMaterial) {
        if (!itemMaterial.getEncomenda().getId().equals(encomenda.getId())) {
            throw new RegraNegocioException("O material informado nao pertence a esta encomenda.");
        }
    }
}
