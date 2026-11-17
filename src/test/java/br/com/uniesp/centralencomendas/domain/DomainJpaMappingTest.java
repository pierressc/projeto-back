package br.com.uniesp.centralencomendas.domain;

import br.com.uniesp.centralencomendas.domain.entity.AgendaSemana;
import br.com.uniesp.centralencomendas.domain.entity.Cliente;
import br.com.uniesp.centralencomendas.domain.entity.Colaborador;
import br.com.uniesp.centralencomendas.domain.entity.Encomenda;
import br.com.uniesp.centralencomendas.domain.entity.ItemMaterialEncomenda;
import br.com.uniesp.centralencomendas.domain.entity.MaterialEspecial;
import br.com.uniesp.centralencomendas.domain.entity.ProdutoCatalogo;
import br.com.uniesp.centralencomendas.domain.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.domain.enums.CategoriaProduto;
import br.com.uniesp.centralencomendas.domain.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.domain.enums.SetorColaborador;
import br.com.uniesp.centralencomendas.domain.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.domain.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.domain.enums.TipoPagamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DomainJpaMappingTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void devePersistirECarregarTodasAsEntidadesERelacionamentosJpa() {
        Cliente cliente = entityManager.persistAndFlush(
                new Cliente("Lucas Andrade", "(83) 98888-2222", "lucas@email.com", null)
        );
        Colaborador atendente = entityManager.persistAndFlush(
                new Colaborador("Beatriz Lima", "beatriz@empresa.com", SetorColaborador.ATENDIMENTO)
        );
        Colaborador producao = entityManager.persistAndFlush(
                new Colaborador("Rafael Souza", "rafael@empresa.com", SetorColaborador.PRODUCAO)
        );
        MaterialEspecial papelEspecial = entityManager.persistAndFlush(
                new MaterialEspecial("Papel Kraft 300g", "Papel gramatura alta", "folhas", "Grafica Sul")
        );

        ProdutoCatalogo produto = new ProdutoCatalogo(
                "Caixa Convite Padrinhos",
                "Caixa personalizada com laco",
                CategoriaProduto.CASAMENTO,
                new BigDecimal("45.00"),
                2
        );
        produto.associarMaterialEspecial(papelEspecial);
        entityManager.persistAndFlush(produto);

        AgendaSemana agenda = entityManager.persistAndFlush(
                new AgendaSemana("2026-W48", LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 29), 10)
        );

        Encomenda encomenda = new Encomenda(
                "ENC-2026-100",
                cliente,
                atendente,
                agenda,
                CanalOrigem.INSTAGRAM,
                LocalDate.of(2026, 11, 28),
                new BigDecimal("90.00"),
                5,
                TipoEntrega.ENTREGA_DOMICILIO,
                "Rua das Flores, 123 - Joao Pessoa/PB",
                "Entregar no periodo da manha"
        );
        encomenda.adicionarItem(produto, 4, new BigDecimal("45.00"), "Iniciais M&J em dourado");
        ItemMaterialEncomenda matItem = encomenda.adicionarMaterialEspecial(
                papelEspecial,
                new BigDecimal("12.00"),
                "Folhas para base e tampa"
        );
        encomenda.confirmarCompraMaterial(matItem);
        encomenda.concluirChecagemMateriais();
        encomenda.registrarPagamento(
                new BigDecimal("90.00"),
                TipoPagamento.SINAL,
                FormaPagamento.PIX,
                atendente,
                "50% antecipado"
        );
        encomenda.liberarParaProducao();

        var alteracao = encomenda.solicitarAlteracao(
                "Incluir tag de agradecimento extra",
                new BigDecimal("20.00"),
                atendente,
                LocalDate.of(2026, 11, 24)
        );
        encomenda.aprovarSolicitacaoAlteracao(alteracao, producao, new BigDecimal("20.00"), "Viavel");

        entityManager.persistAndFlush(encomenda);
        Long encomendaId = encomenda.getId();
        entityManager.clear();

        Encomenda carregada = entityManager.find(Encomenda.class, encomendaId);
        assertThat(carregada).isNotNull();
        assertThat(carregada.getStatus()).isEqualTo(StatusEncomenda.EM_PRODUCAO);
        assertThat(carregada.getCliente().getNome()).isEqualTo("Lucas Andrade");
        assertThat(carregada.getAtendente().getNome()).isEqualTo("Beatriz Lima");
        assertThat(carregada.getAgendaSemana().getPedidosReservados()).isEqualTo(1);
        assertThat(carregada.getItens()).hasSize(1);
        assertThat(carregada.getItens().getFirst().getProduto().getMateriaisEspeciais()).hasSize(1);
        assertThat(carregada.getMateriaisEspeciais()).hasSize(1);
        assertThat(carregada.getPagamentos()).hasSize(1);
        assertThat(carregada.getSolicitacoesAlteracao()).hasSize(1);
        assertThat(carregada.getEntrega().getTipo()).isEqualTo(TipoEntrega.ENTREGA_DOMICILIO);
        assertThat(carregada.getValorTotal()).isEqualByComparingTo("200.00");
        assertThat(carregada.calcularSaldoAReceber()).isEqualByComparingTo("110.00");
    }
}
