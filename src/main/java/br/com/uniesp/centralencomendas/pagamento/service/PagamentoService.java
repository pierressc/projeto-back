package br.com.uniesp.centralencomendas.pagamento.service;

import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.service.ColaboradorService;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.service.EncomendaService;
import br.com.uniesp.centralencomendas.pagamento.dto.PagamentoResponse;
import br.com.uniesp.centralencomendas.pagamento.dto.RegistrarPagamentoRequest;
import br.com.uniesp.centralencomendas.pagamento.entity.Pagamento;
import br.com.uniesp.centralencomendas.pagamento.repository.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final EncomendaService encomendaService;
    private final ColaboradorService colaboradorService;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            EncomendaService encomendaService,
            ColaboradorService colaboradorService
    ) {
        this.pagamentoRepository = pagamentoRepository;
        this.encomendaService = encomendaService;
        this.colaboradorService = colaboradorService;
    }

    @Transactional
    public PagamentoResponse registrar(Long encomendaId, RegistrarPagamentoRequest request) {
        Encomenda encomenda = encomendaService.buscarEntidade(encomendaId);
        Colaborador responsavel = colaboradorService.buscarEntidade(request.registradoPorId());

        Pagamento pagamento = encomenda.registrarPagamento(
                request.valor(),
                request.forma(),
                responsavel,
                request.observacao()
        );
        return PagamentoResponse.from(pagamentoRepository.save(pagamento));
    }

    @Transactional(readOnly = true)
    public List<PagamentoResponse> listarPorEncomenda(Long encomendaId) {
        encomendaService.buscarEntidade(encomendaId);
        return pagamentoRepository.findByEncomendaIdOrderByDataPagamentoAsc(encomendaId)
                .stream()
                .map(PagamentoResponse::from)
                .toList();
    }
}
