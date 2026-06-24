# Colecao Postman

Esta pasta contem a evidencia executavel de testes da API Raizes do Nordeste.

Arquivos:

- `Raizes_do_Nordeste_Backend.postman_collection.json`
- `Raizes_do_Nordeste_Local.postman_environment.json`

## Como executar

1. Suba a API localmente com `.\gradlew.bat bootRun`.
2. Importe a colecao no Postman.
3. Importe o environment local.
4. Selecione o environment `Raizes do Nordeste - Local`.
5. Execute as pastas na ordem em que aparecem na colecao.

## Cobertura

A colecao cobre login admin, cadastro e consulta de cliente, login de cliente,
listagem de unidades, cardapio, pedidos, canal `WEB`, pagamento mock, `401`
padronizado, `403` padronizado, propriedade de pedido, bloqueio de alteracao de
status/desconto por cliente, pontos zero/negativos, produto inexistente,
estoque insuficiente e pagamento recusado.

O request `T08 - Filtrar pedidos por canal APP` valida o contrato paginado de `GET /pedidos`, incluindo `content`, `page`, `limit`, `totalElements` e `totalPages`. As paginas iniciam em zero, o limite padrao e 10 e o maximo permitido e 100.

Os scripts salvam automaticamente `accessToken`, `clientToken`, `clienteId`, `unidadeId`, `produtoId`, `pedidoId`, `pedidoRecusadoId`, `pagamentoId` e `pagamentoRecusadoId`.

## Auditoria

Nao ha endpoint publico para consultar auditoria. A evidencia de auditoria e registrada pela propria API na tabela `auditoria` por meio de `AuditoriaService`.

Os cenarios da colecao que geram registros de auditoria incluem:

- `T06 - Criar pedido valido`
- `T13 - Pagamento mock aprovado`
- `T16 - Pagamento mock recusado`
- `T17 - Consultar pedido cancelado apos pagamento recusado`, como verificacao do fluxo que repoe estoque e registra atualizacao do pedido

Para comprovar via banco local, apos executar a colecao consulte:

```sql
SELECT id, acao, entidade, usuario, data_hora, valor_novo
FROM auditoria
ORDER BY id DESC;
```

Capture o resultado dessa consulta junto da request/response do pagamento mock.
Esse resultado depende do PostgreSQL local e nao deve ser substituido por dado
simulado. Tambem e possivel observar os logs da aplicacao durante o fluxo.
