package br.com.uniesp.centralencomendas.encomenda.service;

import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import br.com.uniesp.centralencomendas.cliente.service.ClienteService;
import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.service.ColaboradorService;
import br.com.uniesp.centralencomendas.encomenda.dto.CriarEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.ItemEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.dto.MaterialEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.entity.ItemEncomenda;
import br.com.uniesp.centralencomendas.encomenda.entity.MaterialEncomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class EncomendaService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale LOCALE_PT_BR = Locale.of("pt", "BR");

    private final EncomendaRepository encomendaRepository;
    private final ClienteService clienteService;
    private final ColaboradorService colaboradorService;

    public EncomendaService(
            EncomendaRepository encomendaRepository,
            ClienteService clienteService,
            ColaboradorService colaboradorService
    ) {
        this.encomendaRepository = encomendaRepository;
        this.clienteService = clienteService;
        this.colaboradorService = colaboradorService;
    }

    @Transactional
    public EncomendaResponse criar(CriarEncomendaRequest request) {
        Cliente cliente = clienteService.buscarEntidade(request.clienteId());
        Colaborador atendente = colaboradorService.buscarEntidade(request.atendenteId());

        Encomenda encomenda = new Encomenda(
                request.codigo(),
                cliente,
                atendente,
                request.canalOrigem(),
                request.dataPrazoEntrega(),
                request.tipoEntrega(),
                request.enderecoEntrega(),
                request.valorSinalExigido(),
                request.observacoes()
        );
        return EncomendaResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional(readOnly = true)
    public List<EncomendaResponse> listar(StatusEncomenda status) {
        List<Encomenda> encomendas = (status == null)
                ? encomendaRepository.findAllByOrderByDataPrazoEntregaAsc()
                : encomendaRepository.findByStatusOrderByDataPrazoEntregaAsc(status);
        return encomendas.stream().map(EncomendaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EncomendaResponse> consultarAgenda(LocalDate inicio, LocalDate fim) {
        return encomendaRepository.findByDataPrazoEntregaBetweenOrderByDataPrazoEntregaAsc(inicio, fim)
                .stream()
                .map(EncomendaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EncomendaResponse buscarPorId(Long id) {
        return EncomendaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public EncomendaResponse adicionarItem(Long encomendaId, ItemEncomendaRequest request) {
        Encomenda encomenda = buscarEntidade(encomendaId);
        encomenda.adicionarItem(
                request.descricaoProduto(),
                request.personalizacao(),
                request.quantidade(),
                request.valorUnitario()
        );
        return EncomendaResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional
    public EncomendaResponse removerItem(Long encomendaId, Long itemId) {
        Encomenda encomenda = buscarEntidade(encomendaId);
        ItemEncomenda item = encomenda.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item nao encontrado: " + itemId));
        encomenda.removerItem(item);
        return EncomendaResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional
    public EncomendaResponse adicionarMaterial(Long encomendaId, MaterialEncomendaRequest request) {
        Encomenda encomenda = buscarEntidade(encomendaId);
        encomenda.adicionarMaterial(request.nome(), request.quantidade(), request.observacao());
        return EncomendaResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional
    public EncomendaResponse confirmarCompraMaterial(Long encomendaId, Long materialId) {
        Encomenda encomenda = buscarEntidade(encomendaId);
        MaterialEncomenda material = encomenda.getMateriais().stream()
                .filter(m -> m.getId().equals(materialId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material nao encontrado: " + materialId));
        encomenda.confirmarCompraMaterial(material);
        return EncomendaResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional
    public EncomendaResponse liberarParaProducao(Long encomendaId) {
        Encomenda encomenda = buscarEntidade(encomendaId);
        encomenda.liberarParaProducao();
        return EncomendaResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional
    public EncomendaResponse marcarComoPronto(Long encomendaId) {
        Encomenda encomenda = buscarEntidade(encomendaId);
        encomenda.marcarComoPronto();
        return EncomendaResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional
    public EncomendaResponse concluirEntrega(Long encomendaId) {
        Encomenda encomenda = buscarEntidade(encomendaId);
        encomenda.concluirEntrega();
        return EncomendaResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional
    public EncomendaResponse cancelar(Long encomendaId, String motivo) {
        Encomenda encomenda = buscarEntidade(encomendaId);
        encomenda.cancelar(motivo);
        return EncomendaResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional(readOnly = true)
    public String gerarResumoWhatsApp(Long encomendaId) {
        Encomenda encomenda = buscarEntidade(encomendaId);
        NumberFormat moeda = NumberFormat.getCurrencyInstance(LOCALE_PT_BR);
        StringBuilder sb = new StringBuilder();
        sb.append("*Resumo do Pedido ").append(encomenda.getCodigo()).append("*\n");
        sb.append("Cliente: ").append(encomenda.getCliente().getNome()).append("\n");
        sb.append("Prazo: ").append(encomenda.getDataPrazoEntrega().format(FORMATO_DATA)).append("\n");
        sb.append("Status: ").append(encomenda.getStatus()).append("\n\n*Itens:*\n");
        for (ItemEncomenda item : encomenda.getItens()) {
            sb.append("- ").append(item.getQuantidade()).append("x ")
                    .append(item.getDescricaoProduto()).append(" (").append(item.getPersonalizacao()).append(")\n");
        }
        sb.append("\nTotal: ").append(moeda.format(encomenda.getValorTotal())).append("\n");
        sb.append("Pago: ").append(moeda.format(encomenda.calcularTotalPago())).append("\n");
        sb.append("Saldo a receber: ").append(moeda.format(encomenda.calcularSaldoAReceber()));
        return sb.toString();
    }

    public Encomenda buscarEntidade(Long id) {
        return encomendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada: " + id));
    }
}
