package br.com.uniesp.centralencomendas.consultapedidos;

import br.com.uniesp.centralencomendas.agendasemana.controller.AgendaSemanaController;
import br.com.uniesp.centralencomendas.agendasemana.dto.AgendaSemanaRequest;
import br.com.uniesp.centralencomendas.agendasemana.dto.AgendaSemanaResponse;
import br.com.uniesp.centralencomendas.agendasemana.dto.AjustarCapacidadeRequest;
import br.com.uniesp.centralencomendas.agendasemana.service.AgendaSemanaService;
import br.com.uniesp.centralencomendas.encomenda.controller.EncomendaConsultaController;
import br.com.uniesp.centralencomendas.encomenda.dto.CalendarioEntregaDiaResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaDetalheResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaResumoResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.EntregaCalendarioItemResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.PainelAndamentoResponse;
import br.com.uniesp.centralencomendas.encomenda.service.EncomendaConsultaService;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AgendaSemanaController.class, EncomendaConsultaController.class})
class ConsultaPedidosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AgendaSemanaService agendaSemanaService;

    @MockitoBean
    private EncomendaConsultaService encomendaConsultaService;

    @Test
    void deveGerenciarAgendaSemanaViaEndpointsRest() throws Exception {
        AgendaSemanaRequest request = new AgendaSemanaRequest(
                "2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 10
        );
        AgendaSemanaResponse response = new AgendaSemanaResponse(
                1L, "2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 10, 2, 8, true
        );
        AgendaSemanaResponse ajustada = new AgendaSemanaResponse(
                1L, "2026-W47", LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22), 15, 2, 13, true
        );

        when(agendaSemanaService.criar(any())).thenReturn(response);
        when(agendaSemanaService.listar()).thenReturn(List.of(response));
        when(agendaSemanaService.consultarPorData(LocalDate.of(2026, 11, 20))).thenReturn(response);
        when(agendaSemanaService.ajustarCapacidadeMaxima(eq(1L), any())).thenReturn(ajustada);

        mockMvc.perform(post("/api/agendas-semana")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoSemana").value("2026-W47"))
                .andExpect(jsonPath("$.vagasRestantes").value(8));

        mockMvc.perform(get("/api/agendas-semana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vagasRestantes").value(8));

        mockMvc.perform(get("/api/agendas-semana/por-data?data=2026-11-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoSemana").value("2026-W47"));

        mockMvc.perform(patch("/api/agendas-semana/1/capacidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AjustarCapacidadeRequest(15))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidadeMaximaPedidos").value(15))
                .andExpect(jsonPath("$.vagasRestantes").value(13));
    }

    @Test
    void deveRetornar422QuandoAjusteCapacidadeViolarTrava01() throws Exception {
        when(agendaSemanaService.ajustarCapacidadeMaxima(eq(1L), any()))
                .thenThrow(new RegraNegocioException("A nova capacidade (1) nao pode ser menor que os pedidos ja reservados (3)."));

        mockMvc.perform(patch("/api/agendas-semana/1/capacidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AjustarCapacidadeRequest(1))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void deveConsultarPedidosPainelECalendarioEntregas() throws Exception {
        EncomendaResumoResponse resumo = new EncomendaResumoResponse(
                10L, "ENC-100", 1L, "Maria Oliveira", "Ana Paula", 3L, "2026-W47",
                CanalOrigem.WHATSAPP, StatusEncomenda.EM_PRODUCAO,
                new BigDecimal("250.00"), new BigDecimal("100.00"), new BigDecimal("100.00"), new BigDecimal("150.00"),
                true, TipoEntrega.RETIRADA_LOJA, LocalDate.of(2026, 11, 21), LocalDateTime.now()
        );
        EncomendaDetalheResponse detalhe = new EncomendaDetalheResponse(
                10L, "ENC-100", 1L, "Maria Oliveira", "(83) 99999-1111", 2L, "Ana Paula",
                3L, "2026-W47", CanalOrigem.WHATSAPP, StatusEncomenda.EM_PRODUCAO,
                new BigDecimal("250.00"), new BigDecimal("100.00"), new BigDecimal("100.00"), new BigDecimal("150.00"),
                true, true, true, 3, "Obs", LocalDateTime.now(), LocalDate.of(2026, 11, 21),
                LocalDateTime.now(), null,
                new EncomendaDetalheResponse.EntregaDetalheDto(
                        5L, TipoEntrega.RETIRADA_LOJA, null, LocalDate.of(2026, 11, 21), null, false, null, null
                ),
                List.of(), List.of(), List.of(), List.of()
        );
        PainelAndamentoResponse painel = new PainelAndamentoResponse(
                1L,
                Map.of(StatusEncomenda.EM_PRODUCAO, 1L),
                Map.of(StatusEncomenda.EM_PRODUCAO, List.of(resumo))
        );
        CalendarioEntregaDiaResponse dia = new CalendarioEntregaDiaResponse(
                LocalDate.of(2026, 11, 21),
                1,
                List.of(new EntregaCalendarioItemResponse(
                        5L, 10L, "ENC-100", "Maria Oliveira", StatusEncomenda.EM_PRODUCAO,
                        TipoEntrega.RETIRADA_LOJA, null, LocalDate.of(2026, 11, 21), false, null
                ))
        );

        when(encomendaConsultaService.listar(StatusEncomenda.EM_PRODUCAO, 1L, null, null, null))
                .thenReturn(List.of(resumo));
        when(encomendaConsultaService.detalhar(10L)).thenReturn(detalhe);
        when(encomendaConsultaService.painelAndamento()).thenReturn(painel);
        when(encomendaConsultaService.calendarioEntregas(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 22)))
                .thenReturn(List.of(dia));

        mockMvc.perform(get("/api/encomendas?status=EM_PRODUCAO&clienteId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("ENC-100"));

        mockMvc.perform(get("/api/encomendas/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("ENC-100"))
                .andExpect(jsonPath("$.entrega.tipo").value("RETIRADA_LOJA"));

        mockMvc.perform(get("/api/encomendas/painel-andamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPedidos").value(1))
                .andExpect(jsonPath("$.totaisPorStatus.EM_PRODUCAO").value(1));

        mockMvc.perform(get("/api/encomendas/calendario-entregas?dataInicio=2026-11-16&dataFim=2026-11-22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalEntregas").value(1))
                .andExpect(jsonPath("$[0].entregas[0].codigoEncomenda").value("ENC-100"));
    }
}
