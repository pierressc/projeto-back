package br.com.uniesp.centralencomendas.entrega;

import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;
import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.colaborador.repository.ColaboradorRepository;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.entrega.dto.AlterarModalidadeEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.dto.ConcluirEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.dto.EntregaResponse;
import br.com.uniesp.centralencomendas.entrega.dto.ReagendarEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.repository.EntregaRepository;
import br.com.uniesp.centralencomendas.entrega.service.EntregaService;
import br.com.uniesp.centralencomendas.produtocatalogo.entity.ProdutoCatalogo;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;
import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.shared.enums.TipoPagamento;
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
class EntregaServiceTest {

    @Mock
    private EncomendaRepository encomendaRepository;

    @Mock
    private EntregaRepository entregaRepository;

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @InjectMocks
    private EntregaService service;

    private Encomenda encomenda;
    private Colaborador atendente;
    private Colaborador entregador;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente("Maria Silva", "(83) 99999-0000", "maria@email.com", null);
        atendente = new Colaborador("Ana Paula", "ana@empresa.com", SetorColaborador.ATENDIMENTO);
        ReflectionTestUtils.setField(atendente, "id", 2L);
        entregador = new Colaborador("Joao Marcos", "joao@empresa.com", SetorColaborador.ENTREGA);
        ReflectionTestUtils.setField(entregador, "id", 4L);

        AgendaSemana agenda = new AgendaSemana("2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 5);
        encomenda = new Encomenda(
                "ENC-001", cliente, atendente, agenda, CanalOrigem.WHATSAPP,
                LocalDate.of(2026, 11, 21), new BigDecimal("100.00"), 2, TipoEntrega.RETIRADA_LOJA, null, null
        );
        ReflectionTestUtils.setField(encomenda, "id", 10L);
        ReflectionTestUtils.setField(encomenda.getEntrega(), "id", 8L);

        ProdutoCatalogo produto = new ProdutoCatalogo(
                "Kit Festa", "Personalizado", CategoriaProduto.ANIVERSARIO, new BigDecimal("25.00"), 1
        );
        encomenda.adicionarItem(produto, 10, new BigDecimal("25.00"), "Tema Jardim");
        encomenda.registrarPagamento(new BigDecimal("100.00"), TipoPagamento.SINAL, FormaPagamento.PIX, atendente, null);
        encomenda.concluirChecagemMateriais();
        encomenda.liberarParaProducao();
        encomenda.marcarComoPronto();
    }

    @Test
    void deveBloquearConclusaoComSaldoPendenteEConcluirAposQuitacaoTrava04() {
        when(encomendaRepository.findById(10L)).thenReturn(Optional.of(encomenda));
        when(colaboradorRepository.findById(4L)).thenReturn(Optional.of(entregador));

        assertThatThrownBy(() -> service.concluir(10L, new ConcluirEntregaRequest(4L, "Tentativa")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Trava 04");

        encomenda.registrarPagamento(new BigDecimal("150.00"), TipoPagamento.SALDO_FINAL, FormaPagamento.PIX, atendente, null);
        when(encomendaRepository.saveAndFlush(encomenda)).thenReturn(encomenda);

        EntregaResponse concluida = service.concluir(10L, new ConcluirEntregaRequest(4L, "Recebido por Maria"));
        assertThat(concluida.concluida()).isTrue();
        assertThat(concluida.statusEncomenda()).isEqualTo(StatusEncomenda.ENTREGUE);
        assertThat(concluida.responsavelEntregaNome()).isEqualTo("Joao Marcos");
    }

    @Test
    void deveReagendarEAlterarModalidadeDeEntrega() {
        when(encomendaRepository.findById(10L)).thenReturn(Optional.of(encomenda));
        when(encomendaRepository.saveAndFlush(encomenda)).thenReturn(encomenda);

        EntregaResponse reagendada = service.reagendar(10L, new ReagendarEntregaRequest(LocalDate.of(2026, 11, 22)));
        assertThat(reagendada.dataPrevista()).isEqualTo(LocalDate.of(2026, 11, 22));

        EntregaResponse alterada = service.alterarModalidade(
                10L, new AlterarModalidadeEntregaRequest(TipoEntrega.ENTREGA_DOMICILIO, "Rua B, 45")
        );
        assertThat(alterada.tipo()).isEqualTo(TipoEntrega.ENTREGA_DOMICILIO);
        assertThat(alterada.enderecoEntrega()).isEqualTo("Rua B, 45");
    }
}
