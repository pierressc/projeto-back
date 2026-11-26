package br.com.uniesp.centralencomendas.encomenda.service;

import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;
import br.com.uniesp.centralencomendas.agendasemana.repository.AgendaSemanaRepository;
import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import br.com.uniesp.centralencomendas.cliente.repository.ClienteRepository;
import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.repository.ColaboradorRepository;
import br.com.uniesp.centralencomendas.encomenda.dto.CancelarEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.dto.CriarEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaCadastroResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.ResumoWhatsAppResponse;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EncomendaService {

    private final EncomendaRepository encomendaRepository;
    private final ClienteRepository clienteRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final AgendaSemanaRepository agendaSemanaRepository;

    public EncomendaService(
            EncomendaRepository encomendaRepository,
            ClienteRepository clienteRepository,
            ColaboradorRepository colaboradorRepository,
            AgendaSemanaRepository agendaSemanaRepository
    ) {
        this.encomendaRepository = encomendaRepository;
        this.clienteRepository = clienteRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.agendaSemanaRepository = agendaSemanaRepository;
    }

    @Transactional
    public EncomendaCadastroResponse criar(CriarEncomendaRequest request) {
        if (encomendaRepository.findByCodigoIgnoreCase(request.codigo().trim()).isPresent()) {
            throw new RegraNegocioException("Ja existe encomenda cadastrada com o codigo '" + request.codigo().trim() + "'.");
        }
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado com id " + request.clienteId() + "."));
        Colaborador atendente = colaboradorRepository.findById(request.atendenteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador atendente nao encontrado com id " + request.atendenteId() + "."));
        AgendaSemana agenda = agendaSemanaRepository.findById(request.agendaSemanaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agenda semanal nao encontrada com id " + request.agendaSemanaId() + "."));

        Encomenda encomenda = new Encomenda(
                request.codigo(),
                cliente,
                atendente,
                agenda,
                request.canalOrigem(),
                request.dataPrazoPrometido(),
                request.valorSinalExigido(),
                request.prazoAlteracaoSemCustoDias(),
                request.tipoEntrega(),
                request.enderecoEntrega(),
                request.observacoesGerais()
        );
        return EncomendaCadastroResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional
    public EncomendaCadastroResponse liberarParaProducao(Long encomendaId) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        encomenda.liberarParaProducao();
        return EncomendaCadastroResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional
    public EncomendaCadastroResponse cancelar(Long encomendaId, CancelarEncomendaRequest request) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        encomenda.cancelar(request.motivo());
        return EncomendaCadastroResponse.from(encomendaRepository.save(encomenda));
    }

    @Transactional(readOnly = true)
    public ResumoWhatsAppResponse gerarResumoWhatsApp(Long encomendaId) {
        Encomenda encomenda = buscarEncomenda(encomendaId);
        return new ResumoWhatsAppResponse(encomenda.getId(), encomenda.getCodigo(), encomenda.gerarResumoWhatsApp());
    }

    private Encomenda buscarEncomenda(Long id) {
        return encomendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encomenda nao encontrada com id " + id + "."));
    }
}
