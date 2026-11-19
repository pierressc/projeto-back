package br.com.uniesp.centralencomendas.domain;

import br.com.uniesp.centralencomendas.agendasemana.entity.AgendaSemana;
import br.com.uniesp.centralencomendas.cliente.entity.Cliente;
import br.com.uniesp.centralencomendas.colaborador.entity.Colaborador;
import br.com.uniesp.centralencomendas.encomenda.entity.Encomenda;
import br.com.uniesp.centralencomendas.itemmaterialencomenda.entity.ItemMaterialEncomenda;
import br.com.uniesp.centralencomendas.materialespecial.entity.MaterialEspecial;
import br.com.uniesp.centralencomendas.produtocatalogo.entity.ProdutoCatalogo;
import br.com.uniesp.centralencomendas.shared.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.shared.enums.CategoriaProduto;
import br.com.uniesp.centralencomendas.shared.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.shared.enums.SetorColaborador;
import br.com.uniesp.centralencomendas.shared.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.shared.enums.StatusSolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.shared.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.shared.enums.TipoPagamento;
import br.com.uniesp.centralencomendas.shared.exception.RegraNegocioException;
import br.com.uniesp.centralencomendas.solicitacaoalteracao.entity.SolicitacaoAlteracao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncomendaDomainTest {

    private Cliente cliente;
    private Colaborador atendente;
    private Colaborador producao;
    private Colaborador entregador;
    private AgendaSemana agendaSemana;
    private ProdutoCatalogo produtoKitFesta;
    private MaterialEspecial fitaDourada;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("Maria Oliveira", "(83) 99999-1111", "maria@email.com", "Prefere contato por WhatsApp");
        atendente = new Colaborador("Ana Paula", "ana@empresa.com", SetorColaborador.ATENDIMENTO);
        producao = new Colaborador("Carlos Silva", "carlos@empresa.com", SetorColaborador.PRODUCAO);
        entregador = new Colaborador("Joao Marcos", "joao@empresa.com", SetorColaborador.ENTREGA);

        agendaSemana = new AgendaSemana(
                "2026-W47",
                LocalDate.of(2026, 11, 16),
                LocalDate.of(2026, 11, 22),
                2
        );

        fitaDourada = new MaterialEspecial("Fita Cetim Dourada", "Fita especial 22mm", "metros", "Armarinho Central");
        produtoKitFesta = new ProdutoCatalogo(
                "Kit Festa Personalizado",
                "Caixas e lembrancinhas com tema sob medida",
                CategoriaProduto.ANIVERSARIO,
                new BigDecimal("25.00"),
                1
        );
        produtoKitFesta.associarMaterialEspecial(fitaDourada);
    }

    @Test
    void trava01DeveReservarVagaNaAgendaEBloquearQuandoSemanaLotada() {
        Encomenda primeira = criarEncomendaPadrao("ENC-001", LocalDate.of(2026, 11, 21));
        criarEncomendaPadrao("ENC-002", LocalDate.of(2026, 11, 22));

        assertThat(agendaSemana.getPedidosReservados()).isEqualTo(2);
        assertThat(agendaSemana.possuiVagaDisponivel()).isFalse();

        assertThatThrownBy(() -> criarEncomendaPadrao("ENC-003", LocalDate.of(2026, 11, 22)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Capacidade maxima");

        primeira.cancelar("Cliente desistiu antes da producao");
        assertThat(agendaSemana.getPedidosReservados()).isEqualTo(1);
        assertThat(agendaSemana.possuiVagaDisponivel()).isTrue();
    }

    @Test
    void trava02DeveBloquearLiberacaoParaProducaoSemSinalPagoEMaterialConferido() {
        Encomenda encomenda = criarEncomendaPadrao("ENC-010", LocalDate.of(2026, 11, 21));
        encomenda.adicionarItem(produtoKitFesta, 10, new BigDecimal("25.00"), "Tema Jardim Encantado - Sofia 5 anos");
        ItemMaterialEncomenda itemMaterial = encomenda.adicionarMaterialEspecial(
                fitaDourada,
                new BigDecimal("5.00"),
                "Usar nos lacos principais"
        );

        assertThatThrownBy(encomenda::liberarParaProducao)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("sinal");

        encomenda.registrarPagamento(
                new BigDecimal("100.00"),
                TipoPagamento.SINAL,
                FormaPagamento.PIX,
                atendente,
                "Sinal 40%"
        );

        assertThatThrownBy(encomenda::concluirChecagemMateriais)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("pendentes de compra");

        encomenda.confirmarCompraMaterial(itemMaterial);
        encomenda.concluirChecagemMateriais();
        encomenda.liberarParaProducao();

        assertThat(encomenda.getStatus()).isEqualTo(StatusEncomenda.EM_PRODUCAO);
        assertThat(encomenda.getDataLiberacaoProducao()).isNotNull();
    }

    @Test
    void trava03DeveBloquearEdicaoDiretaEmProducaoEExigirAprovacaoDaProducao() {
        Encomenda encomenda = criarEncomendaLiberadaParaProducao("ENC-020");

        assertThatThrownBy(() -> encomenda.adicionarItem(produtoKitFesta, 2, new BigDecimal("25.00"), "Extra"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Trava 03");

        SolicitacaoAlteracao solicitacao = encomenda.solicitarAlteracao(
                "Trocar cor da fita para rose e incluir tag com nome",
                new BigDecimal("35.00"),
                atendente,
                LocalDate.of(2026, 11, 19)
        );

        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacaoAlteracao.PENDENTE);
        assertThat(solicitacao.isDentroDoPrazoSemCusto()).isFalse();

        assertThatThrownBy(encomenda::marcarComoPronto)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("solicitacoes de alteracao pendentes");

        assertThatThrownBy(() -> encomenda.aprovarSolicitacaoAlteracao(solicitacao, atendente, new BigDecimal("35.00"), "Ok"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Producao");

        encomenda.aprovarSolicitacaoAlteracao(solicitacao, producao, new BigDecimal("35.00"), "Aprovado com compra extra");

        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacaoAlteracao.APROVADA);
        assertThat(encomenda.getValorTotal()).isEqualByComparingTo("285.00");
        assertThat(encomenda.calcularSaldoAReceber()).isEqualByComparingTo("185.00");
    }

    @Test
    void trava04DeveBloquearEntregaComSaldoPendenteELiberarAposQuitacaoTotal() {
        Encomenda encomenda = criarEncomendaLiberadaParaProducao("ENC-030");
        encomenda.marcarComoPronto();

        assertThat(encomenda.calcularSaldoAReceber()).isEqualByComparingTo("150.00");

        assertThatThrownBy(() -> encomenda.concluirEntrega(entregador))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Trava 04");

        encomenda.registrarPagamento(
                new BigDecimal("150.00"),
                TipoPagamento.SALDO_FINAL,
                FormaPagamento.CARTAO_CREDITO,
                entregador,
                "Pago na retirada"
        );

        encomenda.concluirEntrega(entregador);

        assertThat(encomenda.getStatus()).isEqualTo(StatusEncomenda.ENTREGUE);
        assertThat(encomenda.getEntrega().isConcluida()).isTrue();
        assertThat(encomenda.calcularSaldoAReceber()).isEqualByComparingTo("0.00");
    }

    @Test
    void deveGerarResumoWhatsAppComInformacoesDoPedido() {
        Encomenda encomenda = criarEncomendaPadrao("ENC-040", LocalDate.of(2026, 11, 21));
        encomenda.adicionarItem(produtoKitFesta, 4, new BigDecimal("25.00"), "Tema Safari");

        String resumo = encomenda.gerarResumoWhatsApp();

        assertThat(resumo)
                .contains("ENC-040")
                .contains("Maria Oliveira")
                .contains("Kit Festa Personalizado")
                .contains("Tema Safari")
                .contains("21/11/2026");
    }

    private Encomenda criarEncomendaPadrao(String codigo, LocalDate dataPrazo) {
        return new Encomenda(
                codigo,
                cliente,
                atendente,
                agendaSemana,
                CanalOrigem.WHATSAPP,
                dataPrazo,
                new BigDecimal("100.00"),
                5,
                TipoEntrega.RETIRADA_LOJA,
                null,
                "Cliente pediu cuidado especial na embalagem"
        );
    }

    private Encomenda criarEncomendaLiberadaParaProducao(String codigo) {
        Encomenda encomenda = criarEncomendaPadrao(codigo, LocalDate.of(2026, 11, 21));
        encomenda.adicionarItem(produtoKitFesta, 10, new BigDecimal("25.00"), "Tema Jardim Encantado");
        encomenda.concluirChecagemMateriais();
        encomenda.registrarPagamento(
                new BigDecimal("100.00"),
                TipoPagamento.SINAL,
                FormaPagamento.PIX,
                atendente,
                "Sinal pago"
        );
        encomenda.liberarParaProducao();
        return encomenda;
    }
}
