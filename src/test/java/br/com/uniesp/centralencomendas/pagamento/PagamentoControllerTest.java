package br.com.uniesp.centralencomendas.pagamento;

import br.com.uniesp.centralencomendas.pagamento.controller.PagamentoController;
import br.com.uniesp.centralencomendas.pagamento.dto.EstornarPagamentoRequest;
import br.com.uniesp.centralencomendas.pagamento.dto.PagamentoResponse;
import br.com.uniesp.centralencomendas.pagamento.dto.RegistrarPagamentoRequest;
import br.com.uniesp.centralencomendas.pagamento.dto.ResumoFinanceiroResponse;
import br.com.uniesp.centralencomendas.pagamento.service.PagamentoService;
import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.shared.enums.StatusPagamento;
import br.com.uniesp.centralencomendas.shared.enums.TipoPagamento;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(PagamentoController.class)
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PagamentoService pagamentoService;

    @Test
    void deveRegistrarListarEstornarEConsultarResumoFinanceiro() throws Exception {
        RegistrarPagamentoRequest registrarReq = new RegistrarPagamentoRequest(
                new BigDecimal("100.00"), TipoPagamento.SINAL, FormaPagamento.PIX, 2L, "Sinal 40%"
        );
        PagamentoResponse confirmado = new PagamentoResponse(
                50L, 10L, "ENC-100", 2L, "Ana Paula",
                new BigDecimal("100.00"), TipoPagamento.SINAL, FormaPagamento.PIX,
                StatusPagamento.CONFIRMADO, LocalDateTime.now(), "Sinal 40%"
        );
        PagamentoResponse estornado = new PagamentoResponse(
                50L, 10L, "ENC-100", 2L, "Ana Paula",
                new BigDecimal("100.00"), TipoPagamento.SINAL, FormaPagamento.PIX,
                StatusPagamento.ESTORNADO, LocalDateTime.now(), "Sinal 40% | Estornado: Lancamento duplicado"
        );
        ResumoFinanceiroResponse resumo = new ResumoFinanceiroResponse(
                10L, "ENC-100", new BigDecimal("250.00"), new BigDecimal("100.00"),
                true, new BigDecimal("100.00"), new BigDecimal("150.00"), false, List.of(confirmado)
        );

        when(pagamentoService.registrar(eq(10L), any())).thenReturn(confirmado);
        when(pagamentoService.listar(10L)).thenReturn(List.of(confirmado));
        when(pagamentoService.consultarResumoFinanceiro(10L)).thenReturn(resumo);
        when(pagamentoService.estornar(eq(10L), eq(50L), any())).thenReturn(estornado);

        mockMvc.perform(post("/api/encomendas/10/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrarReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50L))
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        mockMvc.perform(get("/api/encomendas/10/pagamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("SINAL"));

        mockMvc.perform(get("/api/encomendas/10/pagamentos/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sinalPago").value(true))
                .andExpect(jsonPath("$.saldoAReceber").value(150.00))
                .andExpect(jsonPath("$.quitadaTrava04").value(false));

        mockMvc.perform(patch("/api/encomendas/10/pagamentos/50/estornar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EstornarPagamentoRequest("Lancamento duplicado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ESTORNADO"));
    }

    @Test
    void deveRetornar422QuandoPagamentoExcederSaldoAReceber() throws Exception {
        RegistrarPagamentoRequest req = new RegistrarPagamentoRequest(
                new BigDecimal("500.00"), TipoPagamento.SALDO_FINAL, FormaPagamento.CARTAO_CREDITO, 2L, null
        );
        when(pagamentoService.registrar(eq(10L), any()))
                .thenThrow(new RegraNegocioException("O valor do pagamento (500.00) excede o saldo a receber (150.00)."));

        mockMvc.perform(post("/api/encomendas/10/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }
}
