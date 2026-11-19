package br.com.uniesp.centralencomendas.itemmaterialencomenda;

import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;
import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.encomenda.repository.EncomendaRepository;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ChecagemMateriaisResponse;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialEncomendaRequest;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialEncomendaResponse;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialObservacaoRequest;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.entity.ItemMaterialEncomenda;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.repository.ItemMaterialEncomendaRepository;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.service.ItemMaterialEncomendaService;
import br.com.uniesp.centralencomendas.materialespecial.entity.MaterialEspecial;
import br.com.uniesp.centralencomendas.materialespecial.repository.MaterialEspecialRepository;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemMaterialEncomendaServiceTest {

    @Mock
    private EncomendaRepository encomendaRepository;

    @Mock
    private MaterialEspecialRepository materialEspecialRepository;

    @Mock
    private ItemMaterialEncomendaRepository itemMaterialRepository;

    @InjectMocks
    private ItemMaterialEncomendaService service;

    private Encomenda encomenda;
    private MaterialEspecial fitaDourada;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente("Maria Silva", "(83) 99999-0000", "maria@email.com", null);
        Colaborador atendente = new Colaborador("Ana Paula", "ana@empresa.com", SetorColaborador.ATENDIMENTO);
        AgendaSemana agenda = new AgendaSemana("2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 5);
        encomenda = new Encomenda(
                "ENC-001",
                cliente,
                atendente,
                agenda,
                CanalOrigem.WHATSAPP,
                LocalDate.of(2026, 11, 21),
                new BigDecimal("50.00"),
                2,
                TipoEntrega.RETIRADA_LOJA,
                null,
                null
        );
        ReflectionTestUtils.setField(encomenda, "id", 1L);

        fitaDourada = new MaterialEspecial("Fita Cetim Dourada", "22mm", "metros", "Armarinho Central");
        ReflectionTestUtils.setField(fitaDourada, "id", 7L);
    }

    @Test
    void deveAdicionarMaterialEspecialAtivoNaEncomenda() {
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(materialEspecialRepository.findById(7L)).thenReturn(Optional.of(fitaDourada));
        when(encomendaRepository.saveAndFlush(encomenda)).thenReturn(encomenda);

        ItemMaterialEncomendaResponse response = service.adicionar(
                1L,
                new ItemMaterialEncomendaRequest(7L, new BigDecimal("5.00"), "Para lacos")
        );

        assertThat(response.encomendaId()).isEqualTo(1L);
        assertThat(response.materialId()).isEqualTo(7L);
        assertThat(response.materialNome()).isEqualTo("Fita Cetim Dourada");
        assertThat(response.comprado()).isFalse();
        assertThat(encomenda.isMateriaisConferidos()).isFalse();
        verify(encomendaRepository).saveAndFlush(encomenda);
    }

    @Test
    void deveBloquearAdicaoDeMaterialInativo() {
        fitaDourada.desativar();
        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(materialEspecialRepository.findById(7L)).thenReturn(Optional.of(fitaDourada));

        assertThatThrownBy(() -> service.adicionar(1L, new ItemMaterialEncomendaRequest(7L, new BigDecimal("2.00"), null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    void deveListarApenasPendentesQuandoSolicitado() {
        ItemMaterialEncomenda item = encomenda.adicionarMaterialEspecial(fitaDourada, new BigDecimal("3.00"), null);
        ReflectionTestUtils.setField(item, "id", 20L);

        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(itemMaterialRepository.findByEncomendaIdAndCompradoFalse(1L)).thenReturn(List.of(item));

        List<ItemMaterialEncomendaResponse> pendentes = service.listar(1L, true);

        assertThat(pendentes).hasSize(1);
        assertThat(pendentes.getFirst().id()).isEqualTo(20L);
        verify(itemMaterialRepository).findByEncomendaIdAndCompradoFalse(1L);
    }

    @Test
    void deveConfirmarCompraEConcluirChecagemTrava02() {
        ItemMaterialEncomenda item = encomenda.adicionarMaterialEspecial(fitaDourada, new BigDecimal("3.00"), null);
        ReflectionTestUtils.setField(item, "id", 20L);

        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(itemMaterialRepository.findById(20L)).thenReturn(Optional.of(item));
        when(encomendaRepository.saveAndFlush(encomenda)).thenReturn(encomenda);

        assertThatThrownBy(() -> service.concluirChecagem(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("pendentes");

        ItemMaterialEncomendaResponse confirmado = service.confirmarCompra(1L, 20L);
        assertThat(confirmado.comprado()).isTrue();
        assertThat(confirmado.dataConfirmacaoCompra()).isNotNull();

        ChecagemMateriaisResponse checagem = service.concluirChecagem(1L);
        assertThat(checagem.materiaisConferidos()).isTrue();
        assertThat(checagem.todosMateriaisComprados()).isTrue();
        assertThat(checagem.pendentesCompra()).isZero();
    }

    @Test
    void deveEstornarConfirmacaoEAtualizarObservacao() {
        ItemMaterialEncomenda item = encomenda.adicionarMaterialEspecial(fitaDourada, new BigDecimal("3.00"), "Inicial");
        item.confirmarCompra();
        ReflectionTestUtils.setField(item, "id", 20L);

        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(itemMaterialRepository.findById(20L)).thenReturn(Optional.of(item));
        when(itemMaterialRepository.saveAndFlush(any(ItemMaterialEncomenda.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemMaterialEncomendaResponse estornado = service.estornarConfirmacao(1L, 20L);
        assertThat(estornado.comprado()).isFalse();
        assertThat(estornado.dataConfirmacaoCompra()).isNull();

        ItemMaterialEncomendaResponse atualizado = service.atualizarObservacao(
                1L, 20L, new ItemMaterialObservacaoRequest("Trocar fornecedor")
        );
        assertThat(atualizado.observacao()).isEqualTo("Trocar fornecedor");
    }

    @Test
    void deveLancarExcecaoQuandoMaterialPertencerAOutraEncomenda() {
        Cliente outroCliente = new Cliente("Joao", "(83) 98888-0000", "joao@email.com", null);
        Colaborador atendente = new Colaborador("Ana", "ana@empresa.com", SetorColaborador.ATENDIMENTO);
        AgendaSemana agenda = new AgendaSemana("2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 5);
        Encomenda outra = new Encomenda(
                "ENC-002", outroCliente, atendente, agenda, CanalOrigem.PRESENCIAL,
                LocalDate.of(2026, 11, 21), BigDecimal.ZERO, 1, TipoEntrega.RETIRADA_LOJA, null, null
        );
        ReflectionTestUtils.setField(outra, "id", 2L);
        ItemMaterialEncomenda itemDeOutra = outra.adicionarMaterialEspecial(fitaDourada, new BigDecimal("1.00"), null);
        ReflectionTestUtils.setField(itemDeOutra, "id", 99L);

        when(encomendaRepository.findById(1L)).thenReturn(Optional.of(encomenda));
        when(itemMaterialRepository.findById(99L)).thenReturn(Optional.of(itemDeOutra));

        assertThatThrownBy(() -> service.estornarConfirmacao(1L, 99L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("nao pertence");
    }

    @Test
    void deveLancarExcecaoQuandoEncomendaNaoEncontrada() {
        when(encomendaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listar(999L, false))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
