# Central de Encomendas

Sistema back-end desenvolvido para a disciplina **Tecnologias Back-End Avançado (UNIESP)**, voltado à organização e ao controle de encomendas personalizadas de uma pequena empresa com 8 colaboradores (Atendimento, Produção, Administração e Entrega).

---

## 1. Descrição do Projeto

Com o crescimento das vendas pelas redes sociais, as encomendas da empresa passaram a ficar espalhadas entre WhatsApp, Instagram, cadernos e planilhas, causando retrabalho por alterações não comunicadas, atrasos por falta de material especial e entregas feitas sem conferência do pagamento restante.

A **Central de Encomendas** resolve esses problemas centralizando cada pedido em um fluxo único (`AGUARDANDO_SINAL_MATERIAL → EM_PRODUCAO → PRONTO → ENTREGUE`) com as 4 proteções propostas pela equipe:

1. **Visão de agenda por prazo:** consulta das entregas agendadas por intervalo de datas antes de prometer novos prazos (`GET /api/encomendas/agenda`).
2. **Liberação segura para a produção:** uma encomenda só entra em `EM_PRODUCAO` após ter ao menos um item, o sinal mínimo pago e todos os materiais especiais marcados como comprados.
3. **Proteção contra mudanças de última hora:** depois de entrar em produção, itens não podem ser alterados diretamente; qualquer mudança exige uma `SolicitacaoAlteracao` avaliada pela produção, somando eventual custo extra ao total do pedido.
4. **Controle de pagamentos parciais e entrega:** o sistema soma os pagamentos registrados, calcula o saldo restante e bloqueia a conclusão da entrega enquanto houver saldo pendente.

Também há um endpoint simples para gerar o texto de resumo do pedido pronto para enviar ao cliente no WhatsApp (`GET /api/encomendas/{id}/resumo-whatsapp`). Robôs de WhatsApp, controle de estoque peça por peça e contabilidade avançada ficaram fora do escopo para manter o sistema simples de usar.

---

## 2. Arquitetura Escolhida e Justificativa

### Arquitetura Adotada: **Arquitetura em Camadas Organizada por Módulo (Layered Architecture / MVC)**

Cada módulo funcional (`cliente`, `colaborador`, `encomenda`, `pagamento` e `solicitacaoalteracao`) segue as camadas clássicas do Spring Boot (`controller` → `service` → `entity` → `repository`, além de `dto`), mantendo as regras de transição de estado encapsuladas dentro das próprias entidades (evitando *setters* genéricos para status, totais e conclusões).

### Justificativa

* **Necessidades do negócio:** trata-se de uma pequena empresa com 8 colaboradores que precisa de uma solução simples, rápida de implantar e de baixo custo de manutenção, sem complexidade de infraestrutura desnecessária.
* **Complexidade do sistema:** a complexidade principal está nas transições de estado do pedido e nas validações de negócio, não em escala distribuída. A arquitetura em camadas atende bem ao problema sem precisar duplicar modelos entre camadas extras (como ocorreria em uma Arquitetura Hexagonal pura).
* **Características da equipe:** o grupo é formado por 5 estudantes desenvolvendo em Java 21, Spring Boot, JPA e MySQL em prazo acadêmico curto. O padrão em camadas organizado por módulo reduz a curva de aprendizado e facilita a divisão paralela de tarefas.

---

## 3. Organização do Código

```text
src/main/java/br/com/uniesp/centralencomendas/
├── CentralEncomendasApplication.java
├── cliente/                    # entity, repository, dto, service, controller
├── colaborador/                # entity, repository, dto, service, controller
├── encomenda/                  # Encomenda, ItemEncomenda, MaterialEncomenda + repository, dto, service, controller
├── pagamento/                  # entity, repository, dto, service, controller
├── solicitacaoalteracao/       # entity, repository, dto, service, controller
└── shared/
    ├── enums/                  # CanalOrigem, FormaPagamento, SetorColaborador, StatusEncomenda, StatusSolicitacaoAlteracao, TipoEntrega
    ├── exception/              # RegraNegocioException, RecursoNaoEncontradoException
    └── web/                    # ErroResposta, GlobalExceptionHandler
```

---

## 4. Modelagem do Domínio e Diagrama UML

| Entidade | Responsabilidade | Relacionamentos JPA |
|---|---|---|
| `Cliente` | Dados de contato do cliente | `@OneToMany` com `Encomenda` |
| `Colaborador` | Funcionários por setor | Referenciado via `@ManyToOne` por `Encomenda`, `Pagamento` e `SolicitacaoAlteracao` |
| `Encomenda` | Pedido principal, prazo/dados de entrega, ciclo de vida e travas de negócio | `@ManyToOne` (`Cliente`, `Colaborador`); `@OneToMany` (`ItemEncomenda`, `MaterialEncomenda`, `Pagamento`, `SolicitacaoAlteracao`) |
| `ItemEncomenda` | Produtos personalizados, quantidade e valor unitário | `@ManyToOne` com `Encomenda` |
| `MaterialEncomenda` | Checagem simples de materiais especiais do pedido | `@ManyToOne` com `Encomenda` |
| `Pagamento` | Pagamentos parciais ou totais vinculados à encomenda | `@ManyToOne` com `Encomenda` e `Colaborador` |
| `SolicitacaoAlteracao` | Controle de mudanças após início da produção | `@ManyToOne` com `Encomenda` e `Colaborador` (solicitante e avaliador) |

![Diagrama de Classes](docs/uml/diagrama-classes-dominio.png)

* Arquivos do diagrama: [`docs/uml/diagrama-classes-dominio.png`](docs/uml/diagrama-classes-dominio.png) e [`docs/uml/diagrama-classes-dominio.puml`](docs/uml/diagrama-classes-dominio.puml).

---

## 5. Principais Endpoints da API

* **Clientes (`/api/clientes`):** `POST`, `GET`, `GET /{id}`, `PUT /{id}`
* **Colaboradores (`/api/colaboradores`):** `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `PATCH /{id}/ativar`, `PATCH /{id}/desativar`
* **Encomendas (`/api/encomendas`):**
  * `POST`, `GET` (`?status=...`), `GET /agenda?inicio=YYYY-MM-DD&fim=YYYY-MM-DD`, `GET /{id}`, `GET /{id}/resumo-whatsapp`
  * `POST /{id}/itens`, `DELETE /{id}/itens/{itemId}`
  * `POST /{id}/materiais`, `PATCH /{id}/materiais/{materialId}/comprar`
  * `PATCH /{id}/liberar-producao`, `PATCH /{id}/pronto`, `PATCH /{id}/entregar`, `PATCH /{id}/cancelar?motivo=...`
* **Pagamentos (`/api/encomendas/{encomendaId}/pagamentos`):** `POST`, `GET`
* **Solicitações de Alteração (`/api/encomendas/{encomendaId}/alteracoes`):** `POST`, `GET`, `PATCH /{alteracaoId}/aprovar`, `PATCH /{alteracaoId}/recusar`

---

## 6. Instruções para Execução

### Pré-requisitos
* **Java 21**
* **Maven 3.9+** (ou IDE com Maven embutido)
* **MySQL 8.x**

### Configuração do Banco de Dados
Configure o acesso ao MySQL em [`src/main/resources/application.yml`](src/main/resources/application.yml) ou pelas variáveis de ambiente `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` e `DB_PASSWORD`. O banco `central_encomendas` é criado automaticamente se ainda não existir.

### Executar os testes (usa H2 em memória em modo MySQL)
```bash
mvn test
```

### Executar a aplicação
```bash
mvn spring-boot:run
```

---

## 7. Equipe

* Pierre Simon
* Kewy Huang
* Renan Mendes
* Caio Augusto
* Wagner
