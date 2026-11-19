package br.com.uniesp.centralencomendas.agendasemana;

import br.com.uniesp.centralencomendas.agendasemana.dto.AgendaSemanaRequest;
import br.com.uniesp.centralencomendas.agendasemana.dto.AgendaSemanaResponse;
import br.com.uniesp.centralencomendas.agendasemana.dto.AjustarCapacidadeRequest;
import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;
import br.com.uniesp.centralencomendas.agendasemana.repository.AgendaSemanaRepository;
import br.com.uniesp.centralencomendas.agendasemana.service.AgendaSemanaService;
import br.com.uniesp.centralencomendas.shared.exception.RecursoNaoEncontradoException;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaSemanaServiceTest {

    @Mock
    private AgendaSemanaRepository agendaSemanaRepository;

    @InjectMocks
    private AgendaSemanaService service;

    @Test
    void deveCriarAgendaSemanaEDevolverVagasRestantes() {
        when(agendaSemanaRepository.findByCodigoSemanaIgnoreCase("2026-W47")).thenReturn(Optional.empty());
        when(agendaSemanaRepository.save(any(AgendaSemana.class))).thenAnswer(inv -> {
            AgendaSemana saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        AgendaSemanaResponse response = service.criar(new AgendaSemanaRequest(
                "2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 6
        ));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.vagasRestantes()).isEqualTo(6);
        assertThat(response.possuiVagaDisponivel()).isTrue();
    }

    @Test
    void deveRejeitarCodigoSemanaDuplicado() {
        AgendaSemana existente = new AgendaSemana("2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 5);
        when(agendaSemanaRepository.findByCodigoSemanaIgnoreCase("2026-W47")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.criar(new AgendaSemanaRequest(
                "2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 5
        ))).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void deveBloquearAjusteCapacidadeAbaixoDePedidosReservados() {
        AgendaSemana agenda = new AgendaSemana("2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 5);
        ReflectionTestUtils.setField(agenda, "id", 1L);
        agenda.reservarVaga();
        agenda.reservarVaga();

        when(agendaSemanaRepository.findById(1L)).thenReturn(Optional.of(agenda));

        assertThatThrownBy(() -> service.ajustarCapacidadeMaxima(1L, new AjustarCapacidadeRequest(1)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("reservados");
    }

    @Test
    void deveLancar404QuandoDataNaoCobertaPorNenhumaAgenda() {
        LocalDate data = LocalDate.of(2026, 12, 25);
        when(agendaSemanaRepository.findByDataCoberta(data)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarPorData(data))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
