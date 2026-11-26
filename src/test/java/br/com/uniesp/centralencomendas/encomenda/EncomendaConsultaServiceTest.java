package br.com.uniesp.centralencomendas.encomenda;

import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;
import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.encomenda.dto.CalendarioEntregaDiaResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaDetalheResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaResumoResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.PainelAndamentoResponse;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.encomenda.service.EncomendaConsultaService;
import br.com.uniesp.centralencomendas.entrega.repository.EntregaRepository;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncomendaConsultaServiceTest {

    @Mock
    private EncomendaRepository encomendaRepository;

    @Mock
    private EntregaRepository entregaRepository;

    @InjectMocks
    private EncomendaConsultaService service;

    private Encomenda enc1;
    private Encomenda enc2;

    @BeforeEach
    void setUp() {
        Cliente cliente1 = new Cliente("Maria Silva", "(83) 99999-1111", "maria@email.com", null);
        ReflectionTestUtils.setField(cliente1, "id", 1L);
        Cliente cliente2 = new Cliente("Joao Souza", "(83) 98888-2222", "joao@email.com", null);
        ReflectionTestUtils.setField(cliente2, "id", 2L);

        Colaborador atendente = new Colaborador("Ana Paula", "ana@empresa.com", SetorColaborador.ATENDIMENTO);
        ReflectionTestUtils.setField(atendente, "id", 10L);

        AgendaSemana agenda = new AgendaSemana("2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 10);
        ReflectionTestUtils.setField(agenda, "id", 5L);

        enc1 = new Encomenda(
                "ENC-001", cliente1, atendente, agenda, CanalOrigem.WHATSAPP,
                LocalDate.of(2026, 11, 20), new BigDecimal("50.00"), 2, TipoEntrega.RETIRADA_LOJA, null, "Obs 1"
        );
        ReflectionTestUtils.setField(enc1, "id", 100L);
        ReflectionTestUtils.setField(enc1.getEntrega(), "id", 200L);

        enc2 = new Encomenda(
                "ENC-002", cliente2, atendente, agenda, CanalOrigem.PRESENCIAL,
                LocalDate.of(2026, 11, 21), BigDecimal.ZERO, 2, TipoEntrega.ENTREGA_DOMICILIO, "Rua A, 123", null
        );
        ReflectionTestUtils.setField(enc2, "id", 101L);
        ReflectionTestUtils.setField(enc2.getEntrega(), "id", 201L);
    }

    @Test
    void deveFiltrarEncomendasPorStatusECliente() {
        when(encomendaRepository.findByStatusOrderByDataPrazoPrometidoAsc(StatusEncomenda.AGUARDANDO_SINAL_MATERIAL))
                .thenReturn(List.of(enc1, enc2));

        List<EncomendaResumoResponse> resultado = service.listar(
                StatusEncomenda.AGUARDANDO_SINAL_MATERIAL, 1L, null, null, null
        );

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().codigo()).isEqualTo("ENC-001");
    }

    @Test
    void deveDetalharEncomendaCompleta() {
        when(encomendaRepository.findById(100L)).thenReturn(Optional.of(enc1));

        EncomendaDetalheResponse detalhe = service.detalhar(100L);

        assertThat(detalhe.codigo()).isEqualTo("ENC-001");
        assertThat(detalhe.clienteNome()).isEqualTo("Maria Silva");
        assertThat(detalhe.entrega().tipo()).isEqualTo(TipoEntrega.RETIRADA_LOJA);
    }

    @Test
    void deveMontarPainelDeAndamentoAgrupadoPorStatus() {
        when(encomendaRepository.findAll()).thenReturn(List.of(enc1, enc2));

        PainelAndamentoResponse painel = service.painelAndamento();

        assertThat(painel.totalPedidos()).isEqualTo(2);
        assertThat(painel.totaisPorStatus().get(StatusEncomenda.AGUARDANDO_SINAL_MATERIAL)).isEqualTo(2L);
        assertThat(painel.totaisPorStatus().get(StatusEncomenda.EM_PRODUCAO)).isZero();
    }

    @Test
    void deveMontarCalendarioDeEntregasAgrupadoPorData() {
        LocalDate inicio = LocalDate.of(2026, 11, 16);
        LocalDate fim = LocalDate.of(2026, 11, 22);
        when(entregaRepository.findByDataPrevistaBetweenOrderByDataPrevistaAsc(inicio, fim))
                .thenReturn(List.of(enc1.getEntrega(), enc2.getEntrega()));

        List<CalendarioEntregaDiaResponse> calendario = service.calendarioEntregas(inicio, fim);

        assertThat(calendario).hasSize(2);
        assertThat(calendario.get(0).data()).isEqualTo(LocalDate.of(2026, 11, 20));
        assertThat(calendario.get(1).entregas().getFirst().codigoEncomenda()).isEqualTo("ENC-002");
    }

    @Test
    void deveValidarPeriodoInvalidoEEncomendaInexistente() {
        assertThatThrownBy(() -> service.calendarioEntregas(LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 16)))
                .isInstanceOf(RegraNegocioException.class);

        when(encomendaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.detalhar(999L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
