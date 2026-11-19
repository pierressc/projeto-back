package br.com.uniesp.centralencomendas.encomenda;

import br.com.uniesp.centralencomendas.encomenda.controller.EncomendaController;
import br.com.uniesp.centralencomendas.encomenda.dto.CancelarEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.dto.CriarEncomendaRequest;
import br.com.uniesp.centralencomendas.encomenda.dto.EncomendaCadastroResponse;
import br.com.uniesp.centralencomendas.encomenda.dto.ResumoWhatsAppResponse;
import br.com.uniesp.centralencomendas.encomenda.service.EncomendaService;
import br.com.uniesp.centralencomendas.itemencomenda.controller.ItemEncomendaController;
import br.com.uniesp.centralencomendas.itemencomenda.dto.ItemEncomendaRequest;
import br.com.uniesp.centralencomendas.itemencomenda.dto.ItemEncomendaResponse;
import br.com.uniesp.centralencomendas.itemencomenda.service.ItemEncomendaService;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.controller.ItemMaterialEncomendaController;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ChecagemMateriaisResponse;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialEncomendaRequest;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialEncomendaResponse;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.dto.ItemMaterialObservacaoRequest;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.service.ItemMaterialEncomendaService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        EncomendaController.class,
        ItemEncomendaController.class,
        ItemMaterialEncomendaController.class
})
class CadastroEncomendasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EncomendaService encomendaService;

    @MockitoBean
    private ItemEncomendaService itemEncomendaService;

    @MockitoBean
    private ItemMaterialEncomendaService itemMaterialService;

    @Test
    void deveCriarEncomendaViaEndpointRest() throws Exception {
        CriarEncomendaRequest request = new CriarEncomendaRequest(
                "ENC-100",
                1L,
                2L,
                3L,
                CanalOrigem.WHATSAPP,
                LocalDate.of(2026, 11, 25),
                new BigDecimal("100.00"),
                3,
                TipoEntrega.RETIRADA_LOJA,
                null,
                "Festa tema jardim"
        );
        EncomendaCadastroResponse response = encomendaResponse(10L, StatusEncomenda.AGUARDANDO_SINAL_MATERIAL, false);
        when(encomendaService.criar(any())).thenReturn(response);

        mockMvc.perform(post("/api/encomendas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.codigo").value("ENC-100"))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_SINAL_MATERIAL"));
    }

    @Test
    void deveRetornar422QuandoLiberacaoProducaoViolarTrava02() throws Exception {
        when(encomendaService.liberarParaProducao(10L))
                .thenThrow(new RegraNegocioException("Trava 02: materiais especiais precisam estar comprados e com checagem concluida."));

        mockMvc.perform(patch("/api/encomendas/10/liberar-producao"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensagem").value("Trava 02: materiais especiais precisam estar comprados e com checagem concluida."));
    }

    @Test
    void deveCancelarEncomendaEGerarResumoWhatsApp() throws Exception {
        EncomendaCadastroResponse cancelada = encomendaResponse(10L, StatusEncomenda.CANCELADO, false);
        when(encomendaService.cancelar(eq(10L), any())).thenReturn(cancelada);
        when(encomendaService.gerarResumoWhatsApp(10L))
                .thenReturn(new ResumoWhatsAppResponse(10L, "ENC-100", "Resumo ENC-100"));

        mockMvc.perform(patch("/api/encomendas/10/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CancelarEncomendaRequest("Cliente desistiu"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        mockMvc.perform(get("/api/encomendas/10/resumo-whatsapp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("ENC-100"))
                .andExpect(jsonPath("$.resumo").value("Resumo ENC-100"));
    }

    @Test
    void deveGerenciarItensDaEncomendaViaEndpointsRest() throws Exception {
        ItemEncomendaRequest request = new ItemEncomendaRequest(5L, 10, new BigDecimal("25.00"), "Tema Jardim - Sofia");
        ItemEncomendaResponse response = new ItemEncomendaResponse(
                100L, 10L, 5L, "Kit Festa", 10, new BigDecimal("25.00"), new BigDecimal("250.00"), "Tema Jardim - Sofia"
        );

        when(itemEncomendaService.adicionar(eq(10L), any())).thenReturn(response);
        when(itemEncomendaService.listar(10L)).thenReturn(List.of(response));
        when(itemEncomendaService.atualizar(eq(10L), eq(100L), any())).thenReturn(response);
        doNothing().when(itemEncomendaService).remover(10L, 100L);

        mockMvc.perform(post("/api/encomendas/10/itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.subtotal").value(250.00));

        mockMvc.perform(get("/api/encomendas/10/itens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produtoNome").value("Kit Festa"));

        mockMvc.perform(put("/api/encomendas/10/itens/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(10));

        mockMvc.perform(delete("/api/encomendas/10/itens/100"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveGerenciarMateriaisEspeciaisEConcluirChecagemTrava02() throws Exception {
        ItemMaterialEncomendaRequest addReq = new ItemMaterialEncomendaRequest(7L, new BigDecimal("5.00"), "Lacos principais");
        ItemMaterialEncomendaResponse pendente = new ItemMaterialEncomendaResponse(
                200L, 10L, 7L, "Fita Cetim Dourada", "metros", new BigDecimal("5.00"), false, null, "Lacos principais"
        );
        ItemMaterialEncomendaResponse comprado = new ItemMaterialEncomendaResponse(
                200L, 10L, 7L, "Fita Cetim Dourada", "metros", new BigDecimal("5.00"), true, LocalDateTime.now(), "Comprado no Armarinho"
        );
        ChecagemMateriaisResponse checagem = new ChecagemMateriaisResponse(10L, "ENC-100", true, true, 1L, 0L);

        when(itemMaterialService.adicionar(eq(10L), any())).thenReturn(pendente);
        when(itemMaterialService.listar(10L, true)).thenReturn(List.of(pendente));
        when(itemMaterialService.confirmarCompra(10L, 200L)).thenReturn(comprado);
        when(itemMaterialService.atualizarObservacao(eq(10L), eq(200L), any())).thenReturn(comprado);
        when(itemMaterialService.estornarConfirmacao(10L, 200L)).thenReturn(pendente);
        when(itemMaterialService.concluirChecagem(10L)).thenReturn(checagem);

        mockMvc.perform(post("/api/encomendas/10/materiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(200L))
                .andExpect(jsonPath("$.comprado").value(false));

        mockMvc.perform(get("/api/encomendas/10/materiais?apenasPendentes=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].materialNome").value("Fita Cetim Dourada"));

        mockMvc.perform(patch("/api/encomendas/10/materiais/200/confirmar-compra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comprado").value(true));

        mockMvc.perform(patch("/api/encomendas/10/materiais/200/observacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemMaterialObservacaoRequest("Comprado no Armarinho"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observacao").value("Comprado no Armarinho"));

        mockMvc.perform(patch("/api/encomendas/10/materiais/200/estornar-confirmacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comprado").value(false));

        mockMvc.perform(patch("/api/encomendas/10/materiais/concluir-checagem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materiaisConferidos").value(true))
                .andExpect(jsonPath("$.pendentesCompra").value(0));
    }

    @Test
    void deveRetornar422QuandoConcluirChecagemComMaterialPendente() throws Exception {
        when(itemMaterialService.concluirChecagem(10L))
                .thenThrow(new RegraNegocioException("Existem materiais especiais pendentes de compra nesta encomenda."));

        mockMvc.perform(patch("/api/encomendas/10/materiais/concluir-checagem"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensagem").value("Existem materiais especiais pendentes de compra nesta encomenda."));
    }

    private EncomendaCadastroResponse encomendaResponse(Long id, StatusEncomenda status, boolean materiaisConferidos) {
        return new EncomendaCadastroResponse(
                id,
                "ENC-100",
                1L,
                "Maria Oliveira",
                2L,
                "Ana Paula",
                3L,
                "2026-W48",
                CanalOrigem.WHATSAPP,
                status,
                new BigDecimal("250.00"),
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("250.00"),
                materiaisConferidos,
                3,
                TipoEntrega.RETIRADA_LOJA,
                LocalDate.of(2026, 11, 25),
                LocalDateTime.now(),
                null,
                "Festa tema jardim"
        );
    }
}
