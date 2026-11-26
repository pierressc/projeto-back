package br.com.uniesp.centralencomendas.domain;

import br.com.uniesp.centralencomendas.domain.entity.Cliente;
import br.com.uniesp.centralencomendas.domain.entity.Colaborador;
import br.com.uniesp.centralencomendas.domain.entity.Encomenda;
import br.com.uniesp.centralencomendas.domain.entity.MaterialEncomenda;
import br.com.uniesp.centralencomendas.domain.entity.SolicitacaoAlteracao;
import br.com.uniesp.centralencomendas.domain.enums.CanalOrigem;
import br.com.uniesp.centralencomendas.domain.enums.FormaPagamento;
import br.com.uniesp.centralencomendas.domain.enums.SetorColaborador;
import br.com.uniesp.centralencomendas.domain.enums.StatusEncomenda;
import br.com.uniesp.centralencomendas.domain.enums.TipoEntrega;
import br.com.uniesp.centralencomendas.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class EncomendaDominioTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void deveAplicarRegrasDeDominioEPersistirEntidadesJpa() {
        Cliente cliente = entityManager.persistAndFlush(
                new Cliente("Maria Oliveira", "(83) 99999-1111", "maria@email.com", null)
        );
        Colaborador atendente = entityManager.persistAndFlush(
                new Colaborador("Ana Paula", "ana@empresa.com", SetorColaborador.ATENDIMENTO)
        );
        Colaborador producao = entityManager.persistAndFlush(
                new Colaborador("Carlos Silva", "carlos@empresa.com", SetorColaborador.PRODUCAO)
        );

        Encomenda encomenda = new Encomenda(
                "ENC-2026-001",
                cliente,
                atendente,
                CanalOrigem.WHATSAPP,
                LocalDate.of(2026, 11, 28),
                TipoEntrega.RETIRADA_LOJA,
                null,
                new BigDecimal("100.00"),
                "Tema Jardim Encantado"
        );
        encomenda.adicionarItem("Kit Festa Personalizado", "Nome Sofia - tons pasteis", 10, new BigDecimal("25.00"));
        MaterialEncomenda fita = encomenda.adicionarMaterial("Fita Cetim Dourada", "5 metros", "Lacos principais");

        // Trava 02: nao libera sem sinal e material comprado
        assertThatThrownBy(encomenda::liberarParaProducao).isInstanceOf(RegraNegocioException.class);

        encomenda.registrarPagamento(new BigDecimal("100.00"), FormaPagamento.PIX, atendente, "Sinal");
        assertThatThrownBy(encomenda::liberarParaProducao).isInstanceOf(RegraNegocioException.class);

        encomenda.confirmarCompraMaterial(fita);
        encomenda.liberarParaProducao();
        assertThat(encomenda.getStatus()).isEqualTo(StatusEncomenda.EM_PRODUCAO);

        // Trava 03: bloqueia edicao direta e exige solicitacao aprovada pela producao
        assertThatThrownBy(() -> encomenda.adicionarItem("Extra", "Extra", 1, new BigDecimal("10.00")))
                .isInstanceOf(RegraNegocioException.class);

        SolicitacaoAlteracao alteracao = encomenda.solicitarAlteracao("Incluir tag extra", new BigDecimal("30.00"), atendente);
        assertThatThrownBy(encomenda::marcarComoPronto).isInstanceOf(RegraNegocioException.class);

        encomenda.aprovarAlteracao(alteracao, producao, new BigDecimal("30.00"), "Viavel");
        encomenda.marcarComoPronto();

        // Trava 04: bloqueia entrega com saldo pendente
        assertThat(encomenda.getValorTotal()).isEqualByComparingTo("280.00");
        assertThat(encomenda.calcularSaldoAReceber()).isEqualByComparingTo("180.00");
        assertThatThrownBy(encomenda::concluirEntrega).isInstanceOf(RegraNegocioException.class);

        encomenda.registrarPagamento(new BigDecimal("180.00"), FormaPagamento.CARTAO_CREDITO, atendente, "Saldo final");
        encomenda.concluirEntrega();

        entityManager.persistAndFlush(encomenda);
        Long id = encomenda.getId();
        entityManager.clear();

        Encomenda carregada = entityManager.find(Encomenda.class, id);
        assertThat(carregada).isNotNull();
        assertThat(carregada.getStatus()).isEqualTo(StatusEncomenda.ENTREGUE);
        assertThat(carregada.getItens()).hasSize(1);
        assertThat(carregada.getMateriais()).hasSize(1);
        assertThat(carregada.getPagamentos()).hasSize(2);
        assertThat(carregada.getSolicitacoesAlteracao()).hasSize(1);
        assertThat(carregada.calcularSaldoAReceber()).isEqualByComparingTo("0.00");
    }
}
