package br.com.uniesp.centralencomendas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CentralEncomendasApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveExecutarFluxoCompletoDaEncomendaPelaApi() throws Exception {
        long clienteId = postAndGetId("/api/clientes", """
                {
                  "nome": "Joana Santos",
                  "telefone": "(83) 98888-0000",
                  "email": "joana@email.com",
                  "observacoes": "Cliente recorrente"
                }
                """);

        long atendenteId = postAndGetId("/api/colaboradores", """
                {
                  "nome": "Beatriz Lima",
                  "email": "beatriz@empresa.com",
                  "setor": "ATENDIMENTO"
                }
                """);

        long producaoId = postAndGetId("/api/colaboradores", """
                {
                  "nome": "Rafael Costa",
                  "email": "rafael@empresa.com",
                  "setor": "PRODUCAO"
                }
                """);

        long encomendaId = postAndGetId("/api/encomendas", """
                {
                  "codigo": "ENC-API-001",
                  "clienteId": %d,
                  "atendenteId": %d,
                  "canalOrigem": "WHATSAPP",
                  "dataPrazoEntrega": "2026-12-05",
                  "tipoEntrega": "RETIRADA_LOJA",
                  "valorSinalExigido": 100.00,
                  "observacoes": "Entrega pela manha"
                }
                """.formatted(clienteId, atendenteId));

        mockMvc.perform(post("/api/encomendas/{id}/itens", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricaoProduto": "Caixa Convite Padrinhos",
                                  "personalizacao": "Iniciais A&M dourado",
                                  "quantidade": 4,
                                  "valorUnitario": 50.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(200.00));

        String matJson = mockMvc.perform(post("/api/encomendas/{id}/materiais", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Papel Metalizado Dourado",
                                  "quantidade": "10 folhas",
                                  "observacao": "Gramatura 240g"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long materialId = objectMapper.readTree(matJson).path("materiais").get(0).path("id").asLong();

        // Trava 02: falha antes de sinal e material comprado
        mockMvc.perform(patch("/api/encomendas/{id}/liberar-producao", encomendaId))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post("/api/encomendas/{id}/pagamentos", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "registradoPorId": %d,
                                  "valor": 100.00,
                                  "forma": "PIX",
                                  "observacao": "Sinal 50%%"
                                }
                                """.formatted(atendenteId)))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/encomendas/{id}/materiais/{matId}/comprar", encomendaId, materialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materiais[0].comprado").value(true));

        mockMvc.perform(patch("/api/encomendas/{id}/liberar-producao", encomendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PRODUCAO"));

        // Trava 03: solicita e aprova alteracao com custo extra
        long alteracaoId = postAndGetId("/api/encomendas/" + encomendaId + "/alteracoes", """
                {
                  "solicitanteId": %d,
                  "descricaoMudanca": "Trocar cor do laco para verde oliva",
                  "custoAdicional": 25.00
                }
                """.formatted(atendenteId));

        mockMvc.perform(patch("/api/encomendas/{id}/alteracoes/{altId}/aprovar", encomendaId, alteracaoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "avaliadorId": %d,
                                  "custoExtraConfirmado": 25.00,
                                  "motivo": "Fita disponivel"
                                }
                                """.formatted(producaoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));

        mockMvc.perform(patch("/api/encomendas/{id}/pronto", encomendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRONTO"));

        // Trava 04: bloqueia entrega com saldo restante de R$ 125,00
        mockMvc.perform(patch("/api/encomendas/{id}/entregar", encomendaId))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post("/api/encomendas/{id}/pagamentos", encomendaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "registradoPorId": %d,
                                  "valor": 125.00,
                                  "forma": "CARTAO_DEBITO",
                                  "observacao": "Quitacao na retirada"
                                }
                                """.formatted(atendenteId)))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/encomendas/{id}/entregar", encomendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.saldoAReceber").value(0));

        // Consulta de agenda e resumo WhatsApp
        mockMvc.perform(get("/api/encomendas/agenda")
                        .param("inicio", "2026-12-01")
                        .param("fim", "2026-12-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/encomendas/{id}/resumo-whatsapp", encomendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo", containsString("ENC-API-001")));
    }

    private long postAndGetId(String uri, String body) throws Exception {
        String content = mockMvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(content);
        return json.path("id").asLong();
    }
}
