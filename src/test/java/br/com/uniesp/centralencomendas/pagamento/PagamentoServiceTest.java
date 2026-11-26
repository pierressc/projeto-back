package br.com.uniesp.centralencomendas.pagamento;

import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;
import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.repository.ColaboradorRepository;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.pagamento.dto.EstornarPagamentoRequest;
import br.com.uniesp.centralencomendas.pagamento.dto.PagamentoResponse;
import br.com.uniesp.centralencomendas.pagamento.dto.RegistrarPagamentoRequest;
import br.com.uniesp.centralencomendas.pagamento.dto.ResumoFinanceiroResponse;
import br.com.uniesp.centralencomendas.pagamento.entity.Pagamento;
import br.com.uniesp.centralencomendas.pagamento.repository.PagamentoRepository;
import br.com.uniesp.centralencomendas.pagamento.service.PagamentoService;
import br.com.uniesp.centralencomendas.produtocatalogo.entity.ProdutoCatalogo;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;
import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import br.com.uniesp.centralencomendas.shared.enums.StatusPagamento;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.shared.enums.TipoPagamento;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
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
class PagamentoServiceTest {

    @Mock
    private EncomendaRepository encomendaRepository;

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @InjectMocks
    private PagamentoService service;

    private Encomenda encomenda;
    private Colaborador atendente;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente("Maria Silva", "(83) 99999-0000", "maria@email.com", null);
        atendente = new Colaborador("Ana Paula", "ana@empresa.com", SetorColaborador.ATENDIMENTO);
        ReflectionTestUtils.setField(atendente, "id", 2L);

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
    }

    @Test
    void deveRegistrarSinalESaldoFinalEAtualizarResumoTrava04() {
        when(encomendaRepository.findById(10L)).thenReturn(Optional.of(encomenda));
        when(colaboradorRepository.findById(2L)).thenReturn(Optional.of(atendente));
        when(encomendaRepository.saveAndFlush(encomenda)).thenReturn(encomenda);

        PagamentoResponse sinal = service.registrar(10L, new RegistrarPagamentoRequest(
                new BigDecimal("100.00"), TipoPagamento.SINAL, FormaPagamento.PIX, 2L, "Sinal"
        ));
        assertThat(sinal.status()).isEqualTo(StatusPagamento.CONFIRMADO);

        ResumoFinanceiroResponse parcial = service.consultarResumoFinanceiro(10L);
        assertThat(parcial.sinalPago()).isTrue();
        assertThat(parcial.totalPago()).isEqualByComparingTo("100.00");
        assertThat(parcial.saldoAReceber()).isEqualByComparingTo("150.00");
        assertThat(parcial.quitadaTrava04()).isFalse();

        service.registrar(10L, new RegistrarPagamentoRequest(
                new BigDecimal("150.00"), TipoPagamento.SALDO_FINAL, FormaPagamento.CARTAO_DEBITO, 2L, "Quitacao"
        ));

        ResumoFinanceiroResponse quitado = service.consultarResumoFinanceiro(10L);
        assertThat(quitado.saldoAReceber()).isEqualByComparingTo("0.00");
        assertThat(quitado.quitadaTrava04()).isTrue();
    }

    @Test
    void deveEstornarPagamentoERecalcularSaldoAReceber() {
        Pagamento pagamento = encomenda.registrarPagamento(
                new BigDecimal("100.00"), TipoPagamento.SINAL, FormaPagamento.PIX, atendente, "Sinal"
        );
        ReflectionTestUtils.setField(pagamento, "id", 50L);

        when(encomendaRepository.findById(10L)).thenReturn(Optional.of(encomenda));
        when(pagamentoRepository.findById(50L)).thenReturn(Optional.of(pagamento));
        when(encomendaRepository.saveAndFlush(encomenda)).thenReturn(encomenda);

        PagamentoResponse estornado = service.estornar(10L, 50L, new EstornarPagamentoRequest("Valor incorreto"));
        assertThat(estornado.status()).isEqualTo(StatusPagamento.ESTORNADO);
        assertThat(estornado.observacao()).contains("Estornado: Valor incorreto");

        ResumoFinanceiroResponse resumo = service.consultarResumoFinanceiro(10L);
        assertThat(resumo.sinalPago()).isFalse();
        assertThat(resumo.totalPago()).isEqualByComparingTo("0.00");
        assertThat(resumo.saldoAReceber()).isEqualByComparingTo("250.00");
    }

    @Test
    void deveBloquearPagamentoAcimaDoSaldoEEncomendaInexistente() {
        when(encomendaRepository.findById(10L)).thenReturn(Optional.of(encomenda));
        when(colaboradorRepository.findById(2L)).thenReturn(Optional.of(atendente));

        assertThatThrownBy(() -> service.registrar(10L, new RegistrarPagamentoRequest(
                new BigDecimal("300.00"), TipoPagamento.PARCIAL, FormaPagamento.DINHEIRO, 2L, null
        ))).isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("excede o saldo a receber");

        when(encomendaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.consultarResumoFinanceiro(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
