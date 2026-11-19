package br.com.uniesp.centralencomendas.agendasemana.service;

import br.com.uniesp.centralencomendas.agendasemana.dto.AgendaSemanaRequest;
import br.com.uniesp.centralencomendas.agendasemana.dto.AgendaSemanaResponse;
import br.com.uniesp.centralencomendas.agendasemana.dto.AjustarCapacidadeRequest;
import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;
import br.com.uniesp.centralencomendas.agendasemana.repository.AgendaSemanaRepository;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AgendaSemanaService {

    private final AgendaSemanaRepository agendaSemanaRepository;

    public AgendaSemanaService(AgendaSemanaRepository agendaSemanaRepository) {
        this.agendaSemanaRepository = agendaSemanaRepository;
    }

    @Transactional
    public AgendaSemanaResponse criar(AgendaSemanaRequest request) {
        String codigo = request.codigoSemana().trim();
        if (agendaSemanaRepository.findByCodigoSemanaIgnoreCase(codigo).isPresent()) {
            throw new RegraNegocioException("Ja existe agenda semanal cadastrada com o codigo '" + codigo + "'.");
        }
        AgendaSemana agenda = new AgendaSemana(
                codigo,
                request.dataInicio(),
                request.dataFim(),
                request.capacidadeMaximaPedidos()
        );
        return AgendaSemanaResponse.from(agendaSemanaRepository.save(agenda));
    }

    @Transactional(readOnly = true)
    public List<AgendaSemanaResponse> listar() {
        return agendaSemanaRepository.findAllByOrderByDataInicioAsc().stream()
                .map(AgendaSemanaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgendaSemanaResponse buscarPorId(Long id) {
        return AgendaSemanaResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public AgendaSemanaResponse consultarPorData(LocalDate data) {
        AgendaSemana agenda = agendaSemanaRepository.findByDataCoberta(data)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma agenda semanal encontrada para a data " + data + "."));
        return AgendaSemanaResponse.from(agenda);
    }

    @Transactional
    public AgendaSemanaResponse ajustarCapacidadeMaxima(Long id, AjustarCapacidadeRequest request) {
        AgendaSemana agenda = buscarEntidade(id);
        agenda.ajustarCapacidadeMaxima(request.novaCapacidade());
        return AgendaSemanaResponse.from(agendaSemanaRepository.save(agenda));
    }

    private AgendaSemana buscarEntidade(Long id) {
        return agendaSemanaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agenda semanal nao encontrada com id " + id + "."));
    }
}
