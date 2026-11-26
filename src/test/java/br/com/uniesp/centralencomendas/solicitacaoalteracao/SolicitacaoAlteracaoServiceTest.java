package br.com.uniesp.centralencomendas.solicitacaoalteracao;

import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;
import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.repository.ColaboradorRepository;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.produtocatalogo.entity.ProdutoCatalogo;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;
import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.StatusSolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.shared.enums.TipoPagamento;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.AprovarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.ProducaoProntaResponse;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitacaoAlteracaoResponse;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.entity.SolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.repository.SolicitacaoAlteracaoRepository;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.service.SolicitacaoAlteracaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitacaoAlteracaoServiceTest {

    @Mock
    private EncomendaRepository encomendaRepository;

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @Mock
    private SolicitacaoAlteracaoRepository solicitacaoRepository;

    @InjectMocks
    private SolicitacaoAlteracaoService service;

    private Encomenda encomenda;
    private Colaborador atendente;
    private Colaborador producao;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente("Maria Silva", "(83) 99999-0000", "maria@email.com", null);
        atendente = new Colaborador("Ana Paula", "ana@empresa.com", SetorColaborador.ATENDIMENTO);
        ReflectionTestUtils.setField(atendente, "id", 2L);
        producao = new Colaborador("Carlos Silva", "carlos@empresa.com", SetorColaborador.PRODUCAO);
        ReflectionTestUtils.setField(producao, "id", 3L);

        AgendaSemana agenda = new AgendaSemana("2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 5);
        encomenda = new Encomenda(
                "ENC-001", cliente, atendente, agenda, CanalOrigem.WHATSAPP,
                LocalDate.of(2026, 11, 21), new BigDecimal("100.00"), 2, TipoEntrega.RETIRADA_LOJA, null, null
        );
        ReflectionTestUtils.setField(encomenda, "id", 10L);

        ProdutoCatalogo produto = new ProdutoCatalogo(
                "Kit Festa", "Personalizado", CategoriaProduto.ANIVERSARIO, new BigDecimal("25.00"), 1
        );
        encomenda.adicionarItem(produto, 10, new BigDecimal("25.00"), "Tema Jardim");
        encomenda.registrarPagamento(new BigDecimal("100.00"), TipoPagamento.SINAL, FormaPagamento.PIX, atendente, null);
        encomenda.concluirChecagemMateriais();
        encomenda.liberarParaProducao();
    }

    @Test
    void deveSolicitarAprovarAlteracaoEMarcarProntoQuandoSemPendencias() {
        when(encomendaRepository.findById(10L)).thenReturn(Optional.of(encomenda));
        when(colaboradorRepository.findById(2L)).thenReturn(Optional.of(atendente));
        when(colaboradorRepository.findById(3L)).thenReturn(Optional.of(producao));
        when(encomendaRepository.saveAndFlush(encomenda)).thenReturn(encomenda);

        SolicitacaoAlteracaoResponse solicitada = service.solicitar(10L, new SolicitarAlteracaoRequest(
                "Adicionar 2 caixas", new BigDecimal("40.00"), 2L, LocalDate.of(2026, 11, 20)
        ));
        assertThat(solicitada.status()).isEqualTo(StatusSolicitacaoAlteracao.PENDENTE);

        SolicitacaoAlteracao entity = encomenda.getSolicitacoesAlteracao().getFirst();
        ReflectionTestUtils.setField(entity, "id", 5L);
        when(solicitacaoRepository.findById(5L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.marcarEncomendaComoPronta(10L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("pendentes");

        SolicitacaoAlteracaoResponse aprovada = service.aprovar(
                10L, 5L, new AprovarAlteracaoRequest(3L, new BigDecimal("45.00"), "Aprovado pela producao")
        );
        assertThat(aprovada.status()).isEqualTo(StatusSolicitacaoAlteracao.APROVADA);
        assertThat(aprovada.valorTotalEncomendaAtualizado()).isEqualByComparingTo("295.00");

        ProducaoProntaResponse pronto = service.marcarEncomendaComoPronta(10L);
        assertThat(pronto.status()).isEqualTo(StatusEncomenda.PRONTO);
    }

    @Test
    void deveBloquearAprovacaoPorAtendenteSemPermissao() {
        SolicitacaoAlteracao entity = encomenda.solicitarAlteracao(
                "Trocar cor", new BigDecimal("10.00"), atendente, LocalDate.of(2026, 11, 20)
        );
        ReflectionTestUtils.setField(entity, "id", 5L);

        when(encomendaRepository.findById(10L)).thenReturn(Optional.of(encomenda));
        when(solicitacaoRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(colaboradorRepository.findById(2L)).thenReturn(Optional.of(atendente));

        assertThatThrownBy(() -> service.aprovar(10L, 5L, new AprovarAlteracaoRequest(2L, new BigDecimal("10.00"), null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Producao ou Administracao");
    }
}
