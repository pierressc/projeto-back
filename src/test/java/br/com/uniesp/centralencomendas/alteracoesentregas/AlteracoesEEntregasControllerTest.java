package br.com.uniesp.centralencomendas.alteracoesentregas;

import br.com.uniesp.centralencomendas.entrega.controller.EntregaController;
import br.com.uniesp.centralencomendas.entrega.dto.AlterarModalidadeEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.dto.ConcluirEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.dto.EntregaResponse;
import br.com.uniesp.centralencomendas.entrega.dto.ReagendarEntregaRequest;
import br.com.uniesp.centralencomendas.entrega.service.EntregaService;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.StatusSolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.controller.SolicitacaoAlteracaoController;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.AprovarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.ProducaoProntaResponse;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.RecusarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitacaoAlteracaoResponse;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.dto.SolicitarAlteracaoRequest;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.service.SolicitacaoAlteracaoService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SolicitacaoAlteracaoController.class, EntregaController.class})
class AlteracoesEEntregasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SolicitacaoAlteracaoService solicitacaoService;

    @MockitoBean
    private EntregaService entregaService;

    @Test
    void deveGerenciarSolicitacoesAlteracaoEMarcarPronto() throws Exception {
        SolicitarAlteracaoRequest solicitarReq = new SolicitarAlteracaoRequest(
                "Trocar cor da fita para rose", new BigDecimal("30.00"), 2L, LocalDate.of(2026, 11, 18)
        );
        SolicitacaoAlteracaoResponse pendente = new SolicitacaoAlteracaoResponse(
                5L, 10L, "ENC-100", 2L, "Ana Paula", null, null,
                "Trocar cor da fita para rose", new BigDecimal("30.00"),
                StatusSolicitacaoAlteracao.PENDENTE, false, LocalDateTime.now(), null, null, new BigDecimal("250.00")
        );
        SolicitacaoAlteracaoResponse aprovada = new SolicitacaoAlteracaoResponse(
                5L, 10L, "ENC-100", 2L, "Ana Paula", 3L, "Carlos Silva",
                "Trocar cor da fita para rose", new BigDecimal("35.00"),
                StatusSolicitacaoAlteracao.APROVADA, false, LocalDateTime.now(), LocalDateTime.now(), "Aprovado", new BigDecimal("285.00")
        );
        SolicitacaoAlteracaoResponse recusada = new SolicitacaoAlteracaoResponse(
                5L, 10L, "ENC-100", 2L, "Ana Paula", 3L, "Carlos Silva",
                "Trocar cor da fita para rose", new BigDecimal("30.00"),
                StatusSolicitacaoAlteracao.RECUSADA, false, LocalDateTime.now(), LocalDateTime.now(), "Sem estoque", new BigDecimal("250.00")
        );
        ProducaoProntaResponse pronto = new ProducaoProntaResponse(10L, "ENC-100", StatusEncomenda.PRONTO, LocalDateTime.now());

        when(solicitacaoService.solicitar(eq(10L), any())).thenReturn(pendente);
        when(solicitacaoService.listarPorEncomenda(10L)).thenReturn(List.of(pendente));
        when(solicitacaoService.aprovar(eq(10L), eq(5L), any())).thenReturn(aprovada);
        when(solicitacaoService.recusar(eq(10L), eq(5L), any())).thenReturn(recusada);
        when(solicitacaoService.marcarEncomendaComoPronta(10L)).thenReturn(pronto);

        mockMvc.perform(post("/api/encomendas/10/alteracoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitarReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        mockMvc.perform(get("/api/encomendas/10/alteracoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5L));

        mockMvc.perform(patch("/api/encomendas/10/alteracoes/5/aprovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AprovarAlteracaoRequest(3L, new BigDecimal("35.00"), "Aprovado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.valorTotalEncomendaAtualizado").value(285.00));

        mockMvc.perform(patch("/api/encomendas/10/alteracoes/5/recusar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecusarAlteracaoRequest(3L, "Sem estoque"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECUSADA"));

        mockMvc.perform(patch("/api/encomendas/10/marcar-pronto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRONTO"));
    }

    @Test
    void deveGerenciarEntregaEConcluirComTrava04() throws Exception {
        EntregaResponse inicial = new EntregaResponse(
                8L, 10L, "ENC-100", StatusEncomenda.PRONTO, TipoEntrega.RETIRADA_LOJA,
                null, LocalDate.of(2026, 11, 21), null, false, null, null, null, BigDecimal.ZERO
        );
        EntregaResponse concluida = new EntregaResponse(
                8L, 10L, "ENC-100", StatusEncomenda.ENTREGUE, TipoEntrega.ENTREGA_DOMICILIO,
                "Rua B, 45", LocalDate.of(2026, 11, 22), LocalDateTime.now(), true, 4L, "Joao Marcos", "Entregue", BigDecimal.ZERO
        );

        when(entregaService.consultarPorEncomenda(10L)).thenReturn(inicial);
        when(entregaService.reagendar(eq(10L), any())).thenReturn(inicial);
        when(entregaService.alterarModalidade(eq(10L), any())).thenReturn(concluida);
        when(entregaService.concluir(eq(10L), any())).thenReturn(concluida);

        mockMvc.perform(get("/api/encomendas/10/entrega"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("RETIRADA_LOJA"));

        mockMvc.perform(patch("/api/encomendas/10/entrega/reagendar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReagendarEntregaRequest(LocalDate.of(2026, 11, 22)))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/encomendas/10/entrega/modalidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AlterarModalidadeEntregaRequest(TipoEntrega.ENTREGA_DOMICILIO, "Rua B, 45"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("ENTREGA_DOMICILIO"));

        mockMvc.perform(patch("/api/encomendas/10/entrega/concluir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConcluirEntregaRequest(4L, "Entregue"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluida").value(true))
                .andExpect(jsonPath("$.statusEncomenda").value("ENTREGUE"));
    }

    @Test
    void deveRetornar422QuandoEntregaViolarTrava04() throws Exception {
        when(entregaService.concluir(eq(10L), any()))
                .thenThrow(new RegraNegocioException("Trava 04: entrega bloqueada pois ainda ha saldo pendente de R$ 150,00."));

        mockMvc.perform(patch("/api/encomendas/10/entrega/concluir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConcluirEntregaRequest(4L, null))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }
}
