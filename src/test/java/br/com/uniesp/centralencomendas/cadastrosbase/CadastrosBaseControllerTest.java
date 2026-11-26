package br.com.uniesp.centralencomendas.cadastrosbase;

import br.com.uniesp.centralencomendas.cliente.controller.ClienteController;
import br.com.uniesp.centralencomendas.cliente.dto.ClienteRequest;
import br.com.uniesp.centralencomendas.cliente.dto.ClienteResponse;
import br.com.uniesp.centralencomendas.cliente.service.ClienteService;
import br.com.uniesp.centralencomendas.colaborador.controller.ColaboradorController;
import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorRequest;
import br.com.uniesp.centralencomendas.colaborador.dto.ColaboradorResponse;
import br.com.uniesp.centralencomendas.colaborador.service.ColaboradorService;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ClienteController.class, ColaboradorController.class})
class CadastrosBaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private ColaboradorService colaboradorService;

    @Test
    void deveCriarClienteViaEndpointRest() throws Exception {
        ClienteRequest request = new ClienteRequest("Maria Silva", "(83) 99999-0000", "maria@email.com", "VIP");
        ClienteResponse response = new ClienteResponse(1L, "Maria Silva", "(83) 99999-0000", "maria@email.com", "VIP", LocalDateTime.now());
        when(clienteService.criar(any())).thenReturn(response);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    void deveCriarColaboradorViaEndpointRest() throws Exception {
        ColaboradorRequest request = new ColaboradorRequest("Ana Paula", "ana@empresa.com", SetorColaborador.ATENDIMENTO);
        ColaboradorResponse response = new ColaboradorResponse(2L, "Ana Paula", "ana@empresa.com", SetorColaborador.ATENDIMENTO, true);
        when(colaboradorService.criar(any())).thenReturn(response);

        mockMvc.perform(post("/api/colaboradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.setor").value("ATENDIMENTO"))
                .andExpect(jsonPath("$.ativo").value(true));
    }
}
