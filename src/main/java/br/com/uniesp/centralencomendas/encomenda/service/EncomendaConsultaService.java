package br.com.uniesp.centralencomendas.encomenda.service;

import br.com.uniesp.centralencomendas.encomenda.dto.CalendarioEntregaDiaResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaDetalheResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaResumoResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.EntregaCalendarioItemResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.PainelAndamentoResponse;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.entrega.entity.Entrega;
import br.com.uniesp.centralencomendas.entrega.repository.EntregaRepository;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class EncomendaConsultaService {

    private final EncomendaRepository encomendaRepository;
    private final EntregaRepository entregaRepository;

    public EncomendaConsultaService(
            EncomendaRepository encomendaRepository,
            EntregaRepository entregaRepository
    ) {
        this.encomendaRepository = encomendaRepository;
        this.entregaRepository = entregaRepository;
    }

    public List<EncomendaResumoResponse> listar(
            StatusEncomenda status,
            Long clienteId,
            Long agendaSemanaId,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        validarPeriodoOpcional(dataInicio, dataFim);
        List<Encomenda> base = buscarBasePorFiltroPrincipal(status, clienteId, agendaSemanaId, dataInicio, dataFim);

        return base.stream()
                .filter(e -> status == null || e.getStatus() == status)
                .filter(e -> clienteId == null || e.getCliente().getId().equals(clienteId))
                .filter(e -> agendaSemanaId == null || e.getAgendaSemana().getId().equals(agendaSemanaId))
                .filter(e -> dataInicio == null || !e.getDataPrazoPrometido().isBefore(dataInicio))
                .filter(e -> dataFim == null || !e.getDataPrazoPrometido().isAfter(dataFim))
                .map(EncomendaResumoResponse::from)
                .toList();
    }

    public EncomendaDetalheResponse detalhar(Long id) {
        Encomenda encomenda = encomendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada com id " + id + "."));
        return EncomendaDetalheResponse.from(encomenda);
    }

    public PainelAndamentoResponse painelAndamento() {
        List<Encomenda> todas = encomendaRepository.findAll();

        Map<StatusEncomenda, List<EncomendaResumoResponse>> pedidosPorStatus = new EnumMap<>(StatusEncomenda.class);
        Map<StatusEncomenda, Long> totaisPorStatus = new EnumMap<>(StatusEncomenda.class);
        for (StatusEncomenda status : StatusEncomenda.values()) {
            pedidosPorStatus.put(status, new ArrayList<>());
            totaisPorStatus.put(status, 0L);
        }

        for (Encomenda encomenda : todas) {
            StatusEncomenda status = encomenda.getStatus();
            pedidosPorStatus.get(status).add(EncomendaResumoResponse.from(encomenda));
            totaisPorStatus.put(status, totaisPorStatus.get(status) + 1L);
        }

        return new PainelAndamentoResponse(todas.size(), totaisPorStatus, pedidosPorStatus);
    }

    public List<CalendarioEntregaDiaResponse> calendarioEntregas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new RegraNegocioException("As datas de inicio e fim do calendario sao obrigatorias.");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new RegraNegocioException("A data fim do calendario nao pode ser anterior a data inicio.");
        }

        List<Entrega> entregas = entregaRepository.findByDataPrevistaBetweenOrderByDataPrevistaAsc(dataInicio, dataFim);
        Map<LocalDate, List<EntregaCalendarioItemResponse>> agrupado = new LinkedHashMap<>();
        for (Entrega entrega : entregas) {
            agrupado.computeIfAbsent(entrega.getDataPrevista(), ignored -> new ArrayList<>())
                    .add(EntregaCalendarioItemResponse.from(entrega));
        }

        return agrupado.entrySet().stream()
                .map(entry -> new CalendarioEntregaDiaResponse(entry.getKey(), entry.getValue().size(), entry.getValue()))
                .toList();
    }

    private List<Encomenda> buscarBasePorFiltroPrincipal(
            StatusEncomenda status,
            Long clienteId,
            Long agendaSemanaId,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        if (status != null) {
            return encomendaRepository.findByStatusOrderByDataPrazoPrometidoAsc(status);
        }
        if (clienteId != null) {
            return encomendaRepository.findByClienteIdOrderByDataPedidoDesc(clienteId);
        }
        if (agendaSemanaId != null) {
            return encomendaRepository.findByAgendaSemanaIdOrderByDataPrazoPrometidoAsc(agendaSemanaId);
        }
        if (dataInicio != null && dataFim != null) {
            return encomendaRepository.findByDataPrazoPrometidoBetweenOrderByDataPrazoPrometidoAsc(dataInicio, dataFim);
        }
        return encomendaRepository.findAll();
    }

    private void validarPeriodoOpcional(LocalDate dataInicio, LocalDate dataFim) {
        if ((dataInicio != null && dataFim == null) || (dataInicio == null && dataFim != null)) {
            throw new RegraNegocioException("Informe dataInicio e dataFim em conjunto para filtrar por periodo.");
        }
        if (dataInicio != null && dataFim.isBefore(dataInicio)) {
            throw new RegraNegocioException("A data fim do filtro nao pode ser anterior a data inicio.");
        }
    }
}
