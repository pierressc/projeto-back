# Central de Encomendas — Gestão e Controle de Pedidos Personalizados

Sistema back-end desenvolvido para a disciplina **Tecnologias Back-End Avançado (UNIESP)**, com foco na organização operacional e no controle ponta a ponta de uma pequena empresa de produtos personalizados sob encomenda (aniversários, casamentos, eventos corporativos e datas comemorativas).

---

## 1. Descrição do Projeto e Contexto do Problema

Com o crescimento da demanda nas redes sociais, uma empresa com 8 colaboradores (distribuídos entre **Atendimento**, **Produção**, **Administração** e **Entrega**) perdeu o controle centralizado de suas encomendas, que ficavam espalhadas entre conversas de WhatsApp, Instagram, cadernos e planilhas.

Esse cenário gerou três prejuízos recorrentes:
1. **Produção com informação antiga:** clientes solicitavam alterações no atendimento e a Produção não era notificada a tempo.
2. **Atrasos por falta de insumos e sobrecarga de agenda:** prazos prometidos sem visibilidade da capacidade da semana e pedidos iniciados sem checagem de materiais especiais.
3. **Perda financeira:** entregas realizadas sem conferência do pagamento restante e custos extras de retrabalho absorvidos sem visibilidade.

### A Solução: Central de Encomendas e as 4 Travas de Segurança

Em vez de burocratizar o atendimento ou duplicar trabalho, o sistema centraliza o ciclo de vida do pedido (`AGUARDANDO_SINAL_MATERIAL → EM_PRODUCAO → PRONTO → ENTREGUE`) com **quatro travas de negócio encapsuladas no domínio**:

| Trava de Segurança | Regra Implementada no Domínio | Problema Eliminado |
|---|---|---|
| **01. Visão de Agenda na Hora da Venda** | Ao criar a encomenda, `AgendaSemana.reservarVaga()` valida se a data prometida pertence à semana e se ainda há vagas disponíveis. | Promessas no "achismo" e semanas sobrecarregadas. |
| **02. Liberação Segura para a Produção** | `Encomenda.liberarParaProducao()` bloqueia a entrada em produção enquanto o sinal mínimo não estiver pago **e** todos os materiais especiais não estiverem comprados e conferidos. | Produção parada por falta de material e status falso de "em produção". |
| **03. Proteção contra Mudanças de Última Hora** | Após entrar em `EM_PRODUCAO`, `Encomenda` bloqueia edição direta de itens e exige `SolicitacaoAlteracao` formal aprovada pela Produção, somando eventuais custos extras ao valor total. | Desperdício de material e retrabalho com informação desatualizada. |
| **04. Controle de Pagamentos Parciais e Entrega** | `Encomenda` calcula `calcularSaldoAReceber()` em tempo real e `concluirEntrega()` bloqueia a finalização enquanto houver saldo pendente. | Saída de pedidos sem quitação final e prejuízo oculto. |

Além disso, `Encomenda.gerarResumoWhatsApp()` gera um resumo estruturado do pedido pronto para o atendente copiar e enviar como confirmação ao cliente.

### Escopo Inicial vs. Fora de Escopo

* **Contemplado no sistema:** cadastro único da encomenda com itens personalizados, catálogo de produtos e materiais especiais, visão de capacidade semanal, controle de pagamentos parciais, fluxo de solicitação/aprovação de mudanças, trava de entrega e gerador de resumo para WhatsApp.
* **Intencionalmente fora de escopo (Etapa 1):** robôs automáticos no WhatsApp/Instagram (o atendimento humano continua essencial), controle de estoque peça por peça (foco apenas na checagem dos materiais especiais da encomenda) e sistema contábil complexo.

### Premissas Adotadas para Pontos em Aberto

Como os três pontos operacionais levantados para alinhamento com o proprietário ainda estavam em aberto, o domínio foi modelado de forma flexível e parametrizável:
1. **Capacidade semanal:** controlada por `capacidadeMaximaPedidos` na entidade `AgendaSemana`, mantendo também `pesoCapacidade` em `ProdutoCatalogo` para evolução futura por porte do pedido.
2. **Prazo para alterações sem custo:** parametrizado por encomenda via `prazoAlteracaoSemCustoDias`, permitindo calcular automaticamente `dentroDoPrazoSemCusto` ao registrar uma `SolicitacaoAlteracao`.
3. **Acesso na bancada:** arquitetura preparada para API REST stateless consumível por celular, tablet ou computador na loja (Etapa 3).

---

## 2. Arquitetura Escolhida e Justificativa

### Arquitetura Adotada: **Arquitetura em Camadas com Domínio Rico (*Layered Architecture / Spring MVC*)**

O projeto adota uma **Arquitetura em Camadas** tradicional do ecossistema Spring Boot combinada ao princípio de **Domínio Rico** (*Rich Domain Model*): em vez de entidades anêmicas apenas com getters e setters, as regras de negócio, invariantes e transições de estado ficam encapsuladas nas próprias entidades do pacote `domain`.

### Justificativa da Escolha

1. **Alinhamento às necessidades do negócio:**
   O proprietário busca uma solução simples, acessível, de implantação rápida e compatível com o orçamento de uma pequena empresa de 8 colaboradores. Uma arquitetura em camadas enxuta evita custos operacionais e de infraestrutura desnecessários (como orquestração de microsserviços ou mensageria distribuída), entregando exatamente o controle centralizado necessário desde a primeira versão.

2. **Adequação à complexidade do sistema:**
   A complexidade do problema não está em escala distribuída nem em múltiplos protocolos externos, mas sim na **consistência das regras de negócio** (as 4 Travas de Segurança). Ao concentrar essas regras em entidades de domínio ricas — evitando *setters* genéricos para status, totais e conclusões — garantimos proteção forte contra estados inválidos sem precisar duplicar modelos entre camadas como ocorreria em uma Arquitetura Hexagonal ou Clean Architecture pura com mapeadores completos entre domínio e JPA.

3. **Adequação às características da equipe:**
   A equipe é composta por 5 estudantes trabalhando em prazo acadêmico curto utilizando **Java 21, Spring Boot, Spring Data JPA e MySQL**. A arquitetura em camadas é idiomática no ecossistema Spring, reduz a curva de aprendizado coletiva, facilita revisões de código em Pull Requests e permite dividir o desenvolvimento paralelo com clareza para a próxima etapa (`repository`, `service`, `dto` e `controller`).

---

## 3. Organização do Código (Pacotes por Entidade + Arquitetura em Camadas)

Cada uma das 11 entidades do domínio possui seu próprio pacote modular contendo as camadas `entity`, `repository`, `dto`, `service` e `controller`, apoiadas pelo pacote `shared` (enums, exceções, validações e tratamento global de erros HTTP):

```text
src/main/java/br/com/uniesp/centralencomendas/
├── CentralEncomendasApplication.java        # Ponto de entrada Spring Boot
├── shared/                                  # Recursos compartilhados entre módulos
│   ├── enums/                               # Enumerações de domínio (status, setores, canais, etc.)
│   ├── exception/                           # RegraNegocioException e RecursoNaoEncontradoException
│   ├── validation/                          # ValidacaoDominio (invariantes de domínio)
│   └── web/                                 # GlobalExceptionHandler e ErroResposta
├── agendasemana/                            # {controller, dto, entity, repository, service}
├── cliente/                                 # {controller, dto, entity, repository, service}
├── colaborador/                             # {controller, dto, entity, repository, service}
├── encomenda/                               # {controller, dto, entity, repository, service}
├── entrega/                                 # {controller, dto, entity, repository, service}
├── itemencomenda/                           # {controller, dto, entity, repository, service}
├── itemmaterialencomenda/                   # {controller, dto, entity, repository, service}
├── materialespecial/                        # {controller, dto, entity, repository, service}
├── pagamento/                               # {controller, dto, entity, repository, service}
├── produtocatalogo/                         # {controller, dto, entity, repository, service}
└── solicitacaoalteracao/                    # {controller, dto, entity, repository, service}
```

---

## 4. Modelagem do Domínio e Diagrama UML

### Entidades e Relacionamentos JPA

| Entidade | Papel no Domínio | Principais Relacionamentos JPA |
|---|---|---|
| `Encomenda` | Raiz de agregado; coordena ciclo de vida e as 4 Travas | `@ManyToOne` (`Cliente`, `Colaborador`, `AgendaSemana`); `@OneToMany` (`ItemEncomenda`, `ItemMaterialEncomenda`, `Pagamento`, `SolicitacaoAlteracao`); `@OneToOne` (`Entrega`) |
| `Cliente` | Dados de contato e histórico do solicitante | `@OneToMany` (`Encomenda`) |
| `Colaborador` | Equipe da empresa por setor e permissões de ação | Referenciado via `@ManyToOne` por `Encomenda`, `Pagamento`, `SolicitacaoAlteracao` e `Entrega` |
| `AgendaSemana` | Controle de capacidade semanal de entregas (Trava 01) | `@OneToMany` (`Encomenda`) |
| `ProdutoCatalogo` | Produtos base personalizáveis | `@ManyToMany` com `MaterialEspecial` (`@JoinTable produto_material_especial`) |
| `MaterialEspecial` | Insumos específicos não recorrentes | `@ManyToMany(mappedBy = "materiaisEspeciais")` com `ProdutoCatalogo` |
| `ItemEncomenda` | Itens, quantidade, valor e personalização da encomenda | `@ManyToOne` (`Encomenda`, `ProdutoCatalogo`) |
| `ItemMaterialEncomenda` | Checklist de compra de materiais especiais por pedido (Trava 02) | `@ManyToOne` (`Encomenda`, `MaterialEspecial`) |
| `Pagamento` | Lançamentos parciais/sinal/saldo com suporte a estorno (Trava 04) | `@ManyToOne` (`Encomenda`, `Colaborador`) |
| `SolicitacaoAlteracao` | Controle de mudanças após início da produção (Trava 03) | `@ManyToOne` (`Encomenda`, `Colaborador` solicitante e avaliador) |
| `Entrega` | Modalidade, endereço, agendamento e conclusão bloqueada por saldo | `@OneToOne` (`Encomenda`), `@ManyToOne` (`Colaborador`) |

### Diagrama de Classes UML

![Diagrama de Classes de Domínio](docs/uml/diagrama-classes-dominio.png)

* Arquivos do diagrama no repositório:
  * Imagem: [`docs/uml/diagrama-classes-dominio.png`](docs/uml/diagrama-classes-dominio.png)
  * PDF: [`docs/uml/diagrama-classes-dominio.pdf`](docs/uml/diagrama-classes-dominio.pdf)
  * Fonte PlantUML: [`docs/uml/diagrama-classes-dominio.puml`](docs/uml/diagrama-classes-dominio.puml)

---

## 5. Instruções para Execução

### Pré-requisitos
* **Java 21** (JDK 21)
* **Maven 3.9+** (ou IDE com Maven integrado, como IntelliJ IDEA / VS Code)
* **MySQL 8.x** (local ou via Docker Compose incluído no repositório)

### 1) Subir o banco de dados MySQL (opcional via Docker)
```bash
docker compose up -d
```
O container cria automaticamente o banco `central_encomendas` na porta `3306` (`encomendas_user` / `encomendas_pass`). Caso utilize um MySQL já instalado localmente, basta ajustar as variáveis de ambiente `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` e `DB_PASSWORD` (ou editar [`src/main/resources/application.yml`](src/main/resources/application.yml)).

### 2) Executar os testes automatizados (Regras de Domínio e Mapeamento JPA)
Os testes utilizam banco em memória **H2 em modo de compatibilidade MySQL**, validando todas as entidades, relacionamentos JPA e as 4 Travas de Segurança sem depender de instância externa:
```bash
mvn test
```

### 3) Executar a aplicação
```bash
mvn spring-boot:run
```

---

## 6. Equipe e Divisão das Features (Etapa 3)

| Integrante | Branch | Feature / Responsabilidade |
|---|---|---|
| **Wagner** | `feature/cadastros-base` | Cadastros base (`Cliente`, `Colaborador`, `ProdutoCatalogo`, `MaterialEspecial`) |
| **Pierre Simon** | `feature/cadastro-encomendas` | Fluxo principal de encomendas, itens, materiais especiais, Trava 01/02 e resumo WhatsApp (`Encomenda`, `ItemEncomenda`, `ItemMaterialEncomenda`) |
| **Kewy Huang** | `feature/consulta-pedidos` | Visão de agenda semanal (`AgendaSemana`) e painel/consultas rápidas e calendário de encomendas (`EncomendaConsulta`) |
| **Renan Mendes** | `feature/controle-pagamentos` | Lançamento de sinal/parcelas/saldo final, estorno e resumo financeiro (`Pagamento`) |
| **Caio Augusto** | `feature/alteracoes-e-entregas` | Fluxo de alterações em produção - Trava 03 (`SolicitacaoAlteracao`), conclusão de produção e entrega bloqueada por saldo - Trava 04 (`Entrega`) |
