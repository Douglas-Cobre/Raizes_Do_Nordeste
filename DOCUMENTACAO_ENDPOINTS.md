# Documentação Individual dos Endpoints

Este documento descreve os contratos prioritários da API conforme o comportamento implementado em controllers, DTOs, serviços, `SecurityConfig` e `GlobalExceptionHandler`.

Base local: `http://localhost:8080`

Nos endpoints protegidos, enviar:

```http
Authorization: Bearer <accessToken>
```

## Padrão de erro

Erros de negócio e validação usam:

```json
{
  "error": "VALIDACAO_FALHOU",
  "message": "Um ou mais campos estão inválidos.",
  "details": [
    {
      "field": "itens[0].quantidade",
      "issue": "quantidade deve ser maior que zero."
    }
  ],
  "timestamp": "2026-06-21T10:00:00-03:00",
  "path": "/pedidos",
  "requestId": null
}
```

O campo `details` é preenchido para falhas de validação de body. Nos erros lançados como `BusinessException`, ele é retornado como lista vazia.

Erros do Spring Security seguem o mesmo contrato: `401 NAO_AUTENTICADO` para
token ausente/invalido e `403 ACESSO_NEGADO` para usuario autenticado sem
permissao. As respostas usam `Content-Type: application/json`.

## 1. Autenticação

### Login

**Finalidade:** autenticar cliente ou funcionário e emitir JWT válido por oito horas.

**Método e rota:** `POST /auth/login`

**Autenticação:** pública.

**Perfis permitidos:** não se aplica.

**Path/query parameters:** não possui.

**Request:**

```json
{
  "email": "admin@raizesdonordeste.com",
  "senha": "Admin@123"
}
```

**Response `200`:**

```json
{
  "accessToken": "jwt...",
  "tokenType": "Bearer",
  "expiresIn": 28800,
  "user": {
    "id": 1,
    "nome": "Administrador",
    "perfil": "ADMIN"
  }
}
```

**Status HTTP:** `200`, `401`, `422`.

**Erros relevantes:** `CREDENCIAIS_INVALIDAS`, `VALIDACAO_FALHOU`.

## 2. Clientes

### Cadastrar cliente

**Finalidade:** criar uma conta de cliente e seu cadastro de fidelidade inicial.

**Método e rota:** `POST /clientes`

**Autenticação:** pública.

**Perfis permitidos:** não se aplica.

**Path/query parameters:** não possui.

**Request:**

```json
{
  "nome": "Maria Silva",
  "email": "maria@example.com",
  "senha": "Cliente@123",
  "telefone": "(81) 99999-9999",
  "consentimentoLgpd": true
}
```

**Response `201` — campos principais da entidade retornada:**

```json
{
  "id": 1,
  "nome": "Maria Silva",
  "email": "maria@example.com",
  "telefone": "(81) 99999-9999",
  "consentimentoLgpd": true,
  "dataConsentimento": "2026-06-21T10:00:00"
}
```

**Status HTTP:** `201`, `409`, `422`.

**Erros relevantes:** `EMAIL_JA_CADASTRADO`, `VALIDACAO_FALHOU`.

### Consultar cliente

**Finalidade:** buscar um cliente pelo identificador.

**Método e rota:** `GET /clientes/{id}`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `CLIENTE`.

**Path parameters:** `id` — identificador do cliente.

**Query parameters/request body:** não possui.

**Response `200`:** entidade `Cliente`, com os mesmos campos principais apresentados no cadastro. O campo `senha` e seu hash nunca sao serializados.

**Status HTTP:** `200`, `401`, `403`, `404`.

**Erros relevantes:** `CLIENTE_NAO_ENCONTRADO`.

## 3. Unidades e cardápio

### Listar unidades

**Finalidade:** listar todas as unidades cadastradas.

**Método e rota:** `GET /unidades`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Parâmetros/request body:** não possui.

**Response `200` — item representativo:**

```json
[
  {
    "id": 1,
    "nome": "Unidade Recife",
    "cidade": "Recife",
    "estado": "PE",
    "ativa": true,
    "horarioAbertura": "10:00:00",
    "horarioFechamento": "22:00:00",
    "aceitaApp": true,
    "aceitaTotem": true,
    "aceitaBalcao": true,
    "aceitaPickup": true,
    "cozinhaCompleta": true
  }
]
```

**Status HTTP:** `200`, `401`, `403`.

**Erros relevantes:** não há erro de negócio específico para a listagem.

### Consultar cardápio por unidade

**Finalidade:** listar produtos configurados, ativos e dentro do período de disponibilidade da unidade.

**Método e rota:** `GET /unidades/{unidadeId}/cardapio`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`, `CLIENTE`.

**Path parameters:** `unidadeId` — identificador da unidade.

**Query parameters/request body:** não possui.

**Response `200`:**

```json
[
  {
    "produtoId": 1,
    "unidadeId": 1,
    "nomeProduto": "Baião de Dois",
    "descricaoProduto": "Prato regional",
    "categoria": "PRATO_PRINCIPAL",
    "preco": 28.90,
    "disponivel": true,
    "observacaoRegional": "Receita da casa",
    "dataInicioDisponibilidade": null,
    "dataFimDisponibilidade": null
  }
]
```

**Status HTTP:** `200`, `401`, `403`, `404`, `422`.

**Erros relevantes:** `UNIDADE_OBRIGATORIA`, `UNIDADE_NAO_ENCONTRADA`.

## 4. Produtos

### Listar produtos

**Finalidade:** listar todos os produtos do catálogo global.

**Método e rota:** `GET /produtos?page=0&limit=10&sort=nome&direction=asc`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Query parameters:** `page` inicia em `0`; `limit` aceita de `1` a `100`; `sort` aceita `id`, `nome` ou `preco`; `direction` aceita `asc` ou `desc`. Os valores padrao sao `0`, `10`, `nome` e `asc`.

**Path parameters/request body:** nao possui.

**Response `200` — item representativo:**

```json
{
  "content": [
    {
      "id": 1,
      "nome": "Baiao de Dois",
      "descricao": "Prato regional",
      "disponivel": true,
      "categoria": "PRATO_PRINCIPAL",
      "preco": 28.90
    }
  ],
  "page": 0,
  "limit": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

**Status HTTP:** `200`, `401`, `403`, `422`.

**Erros relevantes:** `PAGINACAO_INVALIDA`, `ORDENACAO_INVALIDA`.

### Cadastrar produto

**Finalidade:** criar um produto no catálogo global.

**Método e rota:** `POST /produtos`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Path/query parameters:** não possui.

**Request:**

```json
{
  "nome": "Baião de Dois",
  "descricao": "Prato regional",
  "disponivel": true,
  "categoria": "PRATO_PRINCIPAL",
  "preco": 28.90
}
```

**Response `201`:** entidade `Produto` criada, com `id`, nome, descrição, disponibilidade, categoria e preço.

**Status HTTP:** `201`, `401`, `403`, `422`.

**Erros relevantes:** `VALIDACAO_FALHOU`.

### Consultar produto

**Finalidade:** buscar um produto pelo identificador.

**Método e rota:** `GET /produtos/{id}`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Path parameters:** `id` — identificador do produto.

**Query parameters/request body:** não possui.

**Response `200`:** entidade `Produto`.

**Status HTTP:** `200`, `401`, `403`, `404`.

**Erros relevantes:** `PRODUTO_NAO_ENCONTRADO`.

## 5. Estoque

### Consultar estoque

**Finalidade:** consultar o saldo de um produto em uma unidade.

**Método e rota:** `GET /estoque?produtoId={produtoId}&unidadeId={unidadeId}`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Query parameters:** `produtoId` e `unidadeId`, ambos obrigatórios.

**Path parameters/request body:** não possui.

**Response `200` — campos principais:**

```json
{
  "id": 1,
  "quantidade": 20,
  "estoqueMinimo": 10,
  "produto": {
    "id": 1,
    "nome": "Baião de Dois"
  },
  "unidade": {
    "id": 1,
    "nome": "Unidade Recife"
  }
}
```

**Status HTTP:** `200`, `401`, `403`, `404`.

**Erros relevantes:** `ESTOQUE_NAO_ENCONTRADO`.

### Baixar estoque

**Finalidade:** subtrair uma quantidade do saldo atual.

**Método e rota:** `PATCH /estoque/baixar`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Query parameters:** `produtoId`, `unidadeId` e `quantidade`, todos obrigatórios.

**Path parameters/request body:** não possui.

**Exemplo:** `PATCH /estoque/baixar?produtoId=1&unidadeId=1&quantidade=2`

**Response `200`:** entidade `Estoque` com o saldo atualizado.

**Status HTTP:** `200`, `401`, `403`, `404`, `409`.

**Erros relevantes:** `ESTOQUE_NAO_ENCONTRADO`, `ESTOQUE_INSUFICIENTE`.

### Repor estoque

**Finalidade:** somar uma quantidade ao saldo atual.

**Método e rota:** `PATCH /estoque/repor`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Query parameters:** `produtoId`, `unidadeId` e `quantidade`, todos obrigatórios.

**Path parameters/request body:** não possui.

**Exemplo:** `PATCH /estoque/repor?produtoId=1&unidadeId=1&quantidade=5`

**Response `200`:** entidade `Estoque` com o saldo atualizado.

**Status HTTP:** `200`, `401`, `403`, `404`.

**Erros relevantes:** `ESTOQUE_NAO_ENCONTRADO`.

### Listar estoque baixo

**Finalidade:** listar registros cuja quantidade atual seja menor que 10.

**Método e rota:** `GET /estoque/baixo`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Parâmetros/request body:** não possui.

**Response `200`:** lista de entidades `Estoque`.

**Status HTTP:** `200`, `401`, `403`.

**Erros relevantes:** não há erro de negócio específico para a listagem.

## 6. Pedidos

### Criar pedido

**Finalidade:** validar cliente, unidade, produtos e estoque, calcular total/desconto e baixar o saldo. `CLIENTE` so pode usar o proprio `clienteId`.

**Método e rota:** `POST /pedidos`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`, `CLIENTE`.

**Path/query parameters:** não possui.

**Request:**

```json
{
  "clienteId": 1,
  "unidadeId": 1,
  "canalPedido": "APP",
  "itens": [
    {
      "produtoId": 1,
      "quantidade": 2
    }
  ]
}
```

Valores aceitos para `canalPedido`: `APP`, `TOTEM`, `BALCAO`, `PICKUP`, `WEB`.

Campo ausente retorna `422 VALIDACAO_FALHOU`. Valor inexistente no JSON
retorna `400 JSON_INVALIDO`.

**Response `201` — campos principais:**

```json
{
  "id": 1,
  "status": "AGUARDANDO_PAGAMENTO",
  "canalPedido": "APP",
  "total": 57.80,
  "desconto": 0.00,
  "dataCriacao": "2026-06-21T10:00:00",
  "cliente": {
    "id": 1
  },
  "unidade": {
    "id": 1
  },
  "itens": [
    {
      "id": 1,
      "quantidade": 2,
      "precoUnitario": 28.90,
      "subtotal": 57.80,
      "produto": {
        "id": 1
      }
    }
  ],
  "pagamento": null
}
```

**Status HTTP:** `201`, `400`, `401`, `403`, `404`, `409`, `422`.

**Erros relevantes:** `JSON_INVALIDO`, `ACESSO_NEGADO`, `CLIENTE_OBRIGATORIO`, `UNIDADE_OBRIGATORIA`, `PRODUTO_OBRIGATORIO`, `CLIENTE_NAO_ENCONTRADO`, `UNIDADE_NAO_ENCONTRADA`, `PRODUTO_NAO_ENCONTRADO`, `PRODUTO_INDISPONIVEL_UNIDADE`, `ESTOQUE_NAO_ENCONTRADO`, `ESTOQUE_INSUFICIENTE`, `PRECO_PRODUTO_INDISPONIVEL`, `DESCONTO_INVALIDO`, `FIDELIDADE_NAO_ENCONTRADA`, `VALIDACAO_FALHOU`.

### Consultar pedido

**Finalidade:** buscar um pedido pelo identificador. `CLIENTE` so pode acessar pedido proprio.

**Método e rota:** `GET /pedidos/{id}`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`, `CLIENTE`.

**Path parameters:** `id` — identificador do pedido.

**Query parameters/request body:** não possui.

**Response `200`:** entidade `Pedido`, no formato principal mostrado na criação.

**Status HTTP:** `200`, `401`, `403`, `404`.

**Erros relevantes:** `PEDIDO_NAO_ENCONTRADO`, `ACESSO_NEGADO`.

### Listar ou filtrar pedidos por canal

**Finalidade:** `ADMIN` e `GERENTE` listam todos os pedidos; `CLIENTE` recebe somente os proprios pedidos. O filtro por canal de origem e opcional.

**Método e rota:** `GET /pedidos?canalPedido={canalPedido}&page=0&limit=10&sort=dataCriacao&direction=desc`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`, `CLIENTE`.

**Query parameters:** `canalPedido` e opcional e aceita `APP`, `TOTEM`, `BALCAO`, `PICKUP`, `WEB`; `page` inicia em `0`; `limit` aceita de `1` a `100`; `sort` aceita `id`, `dataCriacao`, `status` ou `canalPedido`; `direction` aceita `asc` ou `desc`. Os defaults sao `page=0`, `limit=10`, `sort=dataCriacao` e `direction=desc`.

**Path parameters/request body:** não possui.

**Response `200`:** objeto paginado. `content` contem as entidades `Pedido`; `page`, `limit`, `totalElements`, `totalPages`, `first` e `last` fornecem os metadados de navegacao.

**Status HTTP:** `200`, `400`, `401`, `403`, `422`.

**Erros relevantes:** `PAGINACAO_INVALIDA`, `ORDENACAO_INVALIDA`,
`PARAMETRO_INVALIDO`. Valor de enum inexistente retorna `400`, com o campo
afetado e os valores permitidos em `details`.

### Atualizar status do pedido

**Finalidade:** alterar diretamente o status de um pedido.

**Método e rota:** `PATCH /pedidos/{id}/status?status={status}`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Path parameters:** `id` — identificador do pedido.

**Query parameters:** `status` — `CRIADO`, `AGUARDANDO_PAGAMENTO`, `PAGO`, `EM_PREPARO`, `PRONTO`, `FINALIZADO`, `CANCELADO` ou `ERRO_PAGAMENTO`.

**Request body:** não possui.

**Response `200`:** entidade `Pedido` com status atualizado.

**Status HTTP:** `200`, `400`, `401`, `403`, `404`.

**Erros relevantes:** `PEDIDO_NAO_ENCONTRADO`, `ACESSO_NEGADO`, `PARAMETRO_INVALIDO`.

### Aplicar ou remover desconto

**Finalidade:** executar operacoes gerenciais de desconto no pedido.

**Rotas:**

- `PATCH /pedidos/{id}/desconto` com body `{"valorDesconto": 10.00, "motivo": "Ajuste gerencial"}`
- `DELETE /pedidos/{id}/desconto`
- `PATCH /pedidos/{id}/desconto-fidelidade?clienteId={clienteId}`

**Autenticacao:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

Na aplicacao por fidelidade, o `clienteId` deve ser o titular do pedido.

**Status HTTP:** `200`, `401`, `403`, `404`, `409`, `422`.

**Erros relevantes:** `ACESSO_NEGADO`, `PEDIDO_NAO_ENCONTRADO`,
`DESCONTO_INVALIDO`, `DESCONTO_SUPERA_TOTAL`,
`CLIENTE_PEDIDO_DIVERGENTE`, `FIDELIDADE_NAO_ENCONTRADA`.

### Cancelar pedido

**Finalidade:** devolver os itens ao estoque e marcar o pedido como cancelado. `CLIENTE` so pode cancelar pedido proprio nos estados `CRIADO` ou `AGUARDANDO_PAGAMENTO`.

**Método e rota:** `DELETE /pedidos/{id}`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`, `CLIENTE`.

**Path parameters:** `id` — identificador do pedido.

**Query parameters/request body:** não possui.

**Response `204`:** sem body.

**Status HTTP:** `204`, `401`, `403`, `404`, `409`.

**Erros relevantes:** `PEDIDO_NAO_ENCONTRADO`, `ESTOQUE_NAO_ENCONTRADO`, `ACESSO_NEGADO`, `CANCELAMENTO_NAO_PERMITIDO`.

## 7. Pagamentos

### Processar pagamento mock

**Finalidade:** simular aprovação ou recusa e atualizar o pedido correspondente.

**Método e rota:** `POST /pagamentos/mock`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Path/query parameters:** não possui.

**Request:**

```json
{
  "pedidoId": 1,
  "metodoPagamento": "MOCK",
  "aprovado": true
}
```

**Response `200`:**

```json
{
  "id": 1,
  "status": "APROVADO",
  "codigoTransacao": "MOCK-uuid",
  "valor": 57.80,
  "dataPagamento": "2026-06-21T10:05:00",
  "metodoPagamento": "MOCK"
}
```

Quando `aprovado` é `false`, o pagamento recebe `RECUSADO`, o pedido recebe `CANCELADO` e o estoque é devolvido.

**Status HTTP:** `200`, `401`, `403`, `404`, `422`.

**Erros relevantes:** `PEDIDO_NAO_ENCONTRADO`, `VALIDACAO_FALHOU`.

### Consultar pagamento

**Finalidade:** buscar um pagamento pelo identificador.

**Método e rota:** `GET /pagamentos/{id}`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Path parameters:** `id` — identificador do pagamento.

**Query parameters/request body:** não possui.

**Response `200`:** entidade `Pagamento`, no formato mostrado no pagamento mock.

**Status HTTP:** `200`, `401`, `403`, `404`.

**Erros relevantes:** `PAGAMENTO_NAO_ENCONTRADO`.

## 8. Fidelidade

### Consultar fidelidade

**Finalidade:** consultar pontos e nível de fidelidade do cliente.

**Método e rota:** `GET /fidelidade/{clienteId}`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `CLIENTE`.

**Path parameters:** `clienteId` — identificador do cliente.

**Query parameters/request body:** não possui.

**Response `200` — campos principais:**

```json
{
  "id": 1,
  "pontos": 0,
  "nivel": "BRONZE",
  "cliente": {
    "id": 1
  }
}
```

**Status HTTP:** `200`, `401`, `403`, `404`.

**Erros relevantes:** `FIDELIDADE_NAO_ENCONTRADA`.

### Adicionar pontos

**Finalidade:** somar pontos e recalcular o nível do cliente.

**Método e rota:** `PATCH /fidelidade/{clienteId}/adicionar?pontos={pontos}`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `CLIENTE`.

**Path parameters:** `clienteId` — identificador do cliente.

**Query parameters:** `pontos` — quantidade positiva a adicionar.

**Request body:** não possui.

**Response `200`:** entidade `Fidelidade` atualizada.

**Status HTTP:** `200`, `401`, `403`, `404`, `422`.

**Erros relevantes:** `FIDELIDADE_NAO_ENCONTRADA`, `PONTOS_INVALIDOS`, `VALIDACAO_FALHOU`.

### Remover pontos

**Finalidade:** subtrair pontos e recalcular o nível; o saldo é limitado a zero.

**Método e rota:** `PATCH /fidelidade/{clienteId}/remover?pontos={pontos}`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `CLIENTE`.

**Path parameters:** `clienteId` — identificador do cliente.

**Query parameters:** `pontos` — quantidade positiva a remover.

**Request body:** não possui.

**Response `200`:** entidade `Fidelidade` atualizada.

**Status HTTP:** `200`, `401`, `403`, `404`, `422`.

**Erros relevantes:** `FIDELIDADE_NAO_ENCONTRADA`, `PONTOS_INVALIDOS`, `VALIDACAO_FALHOU`. Remover mais pontos que o saldo limita o resultado a zero.

## 9. Relatórios

### Consultar painel gerencial

**Finalidade:** consolidar indicadores mensais, metas, vendas, produtos consumidos e estoque baixo.

**Método e rota:** `GET /relatorios/painel-gerencial`

**Autenticação:** JWT Bearer.

**Perfis permitidos:** `ADMIN`, `GERENTE`.

**Parâmetros/request body:** não possui.

**Response `200` — estrutura resumida:**

```json
{
  "pedidosMesAtual": 20,
  "pedidosCanceladosMes": 2,
  "pagamentosAprovadosMes": 18,
  "pagamentosCanceladosMes": 2,
  "faturamentoMesAtual": 1500.00,
  "descontoTotalMes": 50.00,
  "ticketMedioMes": 83.33,
  "estoqueBaixoQuantidade": 3,
  "percentualMetaPedidos": 20.0,
  "percentualMetaFaturamento": 15.0,
  "vendasPorUnidade": [],
  "vendasPorRegiao": [],
  "produtosMaisConsumidos": [],
  "estoqueBaixo": []
}
```

**Status HTTP:** `200`, `401`, `403`.

**Erros relevantes:** não há erro de negócio específico declarado para este endpoint.

## Teste pelo Swagger e Postman

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Coleção: `postman/Raizes_do_Nordeste_Backend.postman_collection.json`
- Ambiente: `postman/Raizes_do_Nordeste_Local.postman_environment.json`

No Swagger, use **Authorize** e informe `Bearer <accessToken>`. No Postman, importe a coleção e o ambiente e execute primeiro o login correspondente ao perfil necessário.

## Observacoes de contrato

- Entidades JPA ainda sao usadas em varios responses, mas `senha` foi marcada como somente escrita e nao e serializada para `Cliente` ou `Funcionario`.
- Operacoes sensiveis de pedido usam autorizacao por metodo; consultas, criacao, listagem e cancelamento validam propriedade quando o perfil e `CLIENTE`.
- O seed cria fidelidade `BRONZE` com zero pontos de forma idempotente para o cliente de demonstracao.
