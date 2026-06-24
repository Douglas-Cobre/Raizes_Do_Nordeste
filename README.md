# Raizes do Nordeste - Backend

API REST academica para a rede de lanchonetes Raizes do Nordeste. O backend cobre unidades, cardapio, clientes, pedidos, estoque, pagamento mock, fidelidade, relatorios, auditoria e autenticacao com JWT.

O foco desta entrega e permitir que o avaliador execute, teste e entenda o MVP principal: criar um pedido, processar um pagamento mock e acompanhar a mudanca de status.

## Tecnologias

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- H2 para testes automatizados
- Swagger/OpenAPI
- Gradle Wrapper

## Requisitos locais

- JDK 17 instalado
- PostgreSQL instalado e em execucao
- Banco local chamado `raizes_db`
- Porta `8080` livre para a API

O projeto ja inclui Gradle Wrapper, entao nao e necessario instalar o Gradle separadamente.

## Configuracao de ambiente

As principais variaveis usadas pela aplicacao sao:

```text
DB_URL=jdbc:postgresql://localhost:5432/raizes_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=troque-esta-chave-por-uma-chave-segura-com-tamanho-suficiente
BOOTSTRAP_ADMIN_EMAIL=admin@raizesdonordeste.com
BOOTSTRAP_ADMIN_PASSWORD=Admin@123
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
```

Existe um arquivo `.env.example` com esses valores. O Spring Boot, neste projeto, nao carrega `.env` automaticamente. Para usar variaveis customizadas, exporte-as no terminal ou configure-as na IDE.

Exemplo no PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/raizes_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:JWT_SECRET="troque-esta-chave-por-uma-chave-segura-com-tamanho-suficiente"
$env:BOOTSTRAP_ADMIN_EMAIL="admin@raizesdonordeste.com"
$env:BOOTSTRAP_ADMIN_PASSWORD="Admin@123"
$env:JPA_DDL_AUTO="update"
$env:JPA_SHOW_SQL="true"
```

Se nenhuma variavel for configurada, os defaults de `src/main/resources/application.properties` permitem rodar com PostgreSQL local em `localhost:5432`, usuario `postgres`, senha `postgres` e banco `raizes_db`.

## Banco de dados

Crie o banco no PostgreSQL:

```sql
CREATE DATABASE raizes_db;
```

Defaults de desenvolvimento:

```properties
DB_URL=jdbc:postgresql://localhost:5432/raizes_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
JPA_DDL_AUTO=update
```

Neste MVP academico, o schema e criado/atualizado pelo Hibernate em ambiente de desenvolvimento por meio de `JPA_DDL_AUTO=update`.

## Dados iniciais

Ao iniciar a aplicacao, `DataInitializer` cria dados minimos de demonstracao quando eles ainda nao existem.

Credenciais de administrador:

```text
email: admin@raizesdonordeste.com
senha: Admin@123
perfil: ADMIN
```

Cliente de teste:

```text
email: cliente.teste@raizesdonordeste.com
senha: Cliente@123
```

O cliente de teste e sua fidelidade sao criados de forma idempotente pelo seed. Em banco limpo, ele inicia com `0` pontos e nivel `BRONZE`. Reiniciar a aplicacao nao duplica o registro.

Unidade:

```text
nome: Raizes Centro
cidade: Recife
estado: PE
horario: 10:00 as 22:00
```

Produtos:

```text
Baiao de Dois - Prato principal - R$ 28,90
Cuscuz Recheado - Lanche - R$ 18,50
```

Estoque inicial:

```text
Baiao de Dois: 30 unidades
Cuscuz Recheado: 25 unidades
estoque minimo: 5
```

Os IDs podem variar conforme o estado do banco. Antes de montar requests manuais, consulte os endpoints de listagem pelo Swagger.

## Como executar a API

No Windows:

```powershell
.\gradlew.bat bootRun
```

No Linux/Mac:

```bash
./gradlew bootRun
```

Quando a aplicacao subir, a API ficara disponivel em:

```text
http://localhost:8080
```

## Swagger/OpenAPI

Interface Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

Documento OpenAPI em JSON:

```text
http://localhost:8080/v3/api-docs
```

O Swagger reflete os endpoints reais da API e e o caminho recomendado para demonstrar a entrega manualmente.

## Colecao Postman

A colecao executavel de testes esta em `postman/Raizes_do_Nordeste_Backend.postman_collection.json`.

Use o environment `postman/Raizes_do_Nordeste_Local.postman_environment.json` e execute as pastas na ordem da colecao: Auth, Clientes, Unidades e Cardapio, Pedidos, Pagamentos, Autorizacao e Auditoria.

A colecao cobre os fluxos positivos do MVP e cenarios negativos de autenticacao, autorizacao granular de pedidos, propriedade do cliente, pontos invalidos, produto inexistente, estoque insuficiente e pagamento mock recusado. Como nao ha endpoint publico de auditoria, a evidencia e conferida diretamente na tabela `auditoria`, conforme `postman/README.md`.

## Autenticacao

Endpoint de login:

```http
POST /auth/login
```

Exemplo de body:

```json
{
  "email": "admin@raizesdonordeste.com",
  "senha": "Admin@123"
}
```

Use o token retornado nas chamadas protegidas:

```http
Authorization: Bearer <token>
```

No Swagger, clique em `Authorize` e informe o token JWT no formato `Bearer <token>`.

## Fluxo MVP: Pedido -> Pagamento mock -> Status

Sequencia recomendada para demonstracao:

1. Fazer login em `POST /auth/login`.
2. Use o cliente seed ou cadastre outro por `POST /clientes`; ambos possuem fidelidade inicial com `0` pontos e nivel `BRONZE`.
3. Listar unidades em `GET /unidades`.
4. Listar o cardapio da unidade em `GET /unidades/{unidadeId}/cardapio`.
5. Criar pedido em `POST /pedidos`.
6. Consultar o pedido em `GET /pedidos/{id}` e verificar status `AGUARDANDO_PAGAMENTO`.
7. Processar pagamento mock aprovado em `POST /pagamentos/mock`.
8. Consultar o pedido novamente e verificar status `PAGO`.
9. Criar outro pedido e processar pagamento mock recusado para verificar status `CANCELADO`.

Exemplo de criacao de pedido:

```http
POST /pedidos
Authorization: Bearer <token>
Content-Type: application/json
```

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

Observacoes:

- Consulte `clienteId`, `unidadeId` e `produtoId` antes, pois os IDs podem variar.
- Quando autenticado como `CLIENTE`, o `clienteId` do request deve ser o mesmo associado ao e-mail usado no JWT.
- O total do pedido e calculado no backend.
- Ao criar o pedido, o estoque e baixado.
- Se nao houver estoque suficiente, a API retorna erro padronizado.

## Pagamento mock

Endpoint:

```http
POST /pagamentos/mock
```

Pagamento aprovado:

```json
{
  "pedidoId": 1,
  "metodoPagamento": "MOCK",
  "aprovado": true
}
```

Pagamento recusado:

```json
{
  "pedidoId": 1,
  "metodoPagamento": "MOCK",
  "aprovado": false
}
```

Comportamento esperado:

- Pagamento aprovado muda o status do pagamento para `APROVADO`.
- Pagamento aprovado muda o status do pedido para `PAGO`.
- Pagamento recusado muda o status do pagamento para `RECUSADO`.
- Pagamento recusado muda o status do pedido para `CANCELADO`.
- Pagamento recusado devolve o estoque do pedido.
- O processamento registra auditoria.

## Endpoints principais

- `POST /auth/login` - autenticacao e emissao de JWT
- `POST /clientes` - cadastro publico de cliente
- `GET /clientes/{id}` - consulta de cliente
- `GET /unidades` - listagem de unidades
- `GET /unidades/{unidadeId}/cardapio` - cardapio por unidade
- `GET /produtos?page=0&limit=10&sort=nome&direction=asc` - listagem paginada de produtos
- `GET /produto-unidades/unidade/{unidadeId}` - produtos vinculados a uma unidade
- `GET /estoque?produtoId={produtoId}&unidadeId={unidadeId}` - consulta de estoque por produto e unidade
- `GET /estoque/baixo` - itens com estoque baixo
- `POST /pedidos` - criacao de pedido
- `GET /pedidos/{id}` - consulta de pedido
- `GET /pedidos?canalPedido=APP&page=0&limit=10&sort=dataCriacao&direction=desc` - listagem paginada de pedidos, com filtro opcional por `canalPedido`
- `PATCH /pedidos/{id}/status` - atualizacao operacional, restrita a `ADMIN` e `GERENTE`
- `DELETE /pedidos/{id}` - cancelamento; `CLIENTE` somente no proprio pedido ainda nao processado
- `POST /pagamentos/mock` - processamento de pagamento mock
- `GET /pagamentos/{id}` - consulta de pagamento
- `GET /pagamentos/status` - listagem de pagamentos por status
- `GET /fidelidade/{clienteId}` - consulta de fidelidade
- `GET /relatorios/painel-gerencial` - painel gerencial

`CLIENTE` cria, consulta, lista e cancela somente os proprios pedidos. Atualizacao de status e aplicacao/remocao de descontos sao restritas a `ADMIN` e `GERENTE`. O usuario administrador do seed consegue executar os fluxos administrativos.

As paginas sao indexadas a partir de zero. O limite padrao e 10 e o maximo permitido e 100 registros por pagina. Produtos aceitam ordenacao por `id`, `nome` ou `preco`; pedidos aceitam `id`, `dataCriacao`, `status` ou `canalPedido`.

## Erro padronizado

Exemplo de resposta de erro:

```json
{
  "error": "ESTOQUE_INSUFICIENTE",
  "message": "Estoque insuficiente",
  "details": [],
  "timestamp": "2026-01-01T10:00:00-03:00",
  "path": "/pedidos",
  "requestId": null
}
```

Erros de validacao podem trazer detalhes no campo `details`.

Requisicoes sem token ou com JWT invalido retornam `401 NAO_AUTENTICADO`.
Usuarios autenticados sem permissao retornam `403 ACESSO_NEGADO`. Ambos usam
o mesmo envelope JSON acima e `Content-Type: application/json`.

Valores enum invalidos ou JSON malformado retornam `400` no formato
padronizado da API. Campos obrigatorios ausentes ou outras rejeicoes de Bean
Validation retornam `422`.

O canal de pedido aceita `APP`, `TOTEM`, `BALCAO`, `PICKUP` e `WEB`. Em bancos
PostgreSQL criados antes da inclusao de `WEB`, confira se existe uma check
constraint antiga antes de testar:

```sql
SELECT conname, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'pedido'::regclass
  AND contype = 'c';
```

Se houver uma constraint de `canal_pedido` sem `WEB`, ela deve ser recriada
incluindo o novo valor. O `ddl-auto=update` pode nao atualizar constraints
existentes automaticamente.

## Testes

Executar testes:

```powershell
.\gradlew.bat test
```

Forcar reexecucao dos testes:

```powershell
.\gradlew.bat test --rerun-tasks
```

Os testes usam o perfil `test` e banco H2 em memoria, definido em `src/test/resources/application-test.properties`. Portanto, os testes automatizados nao dependem do PostgreSQL local.

## Validacao de build

Compilar o projeto:

```powershell
.\gradlew.bat compileJava
```

Validacao recomendada antes da entrega:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test
```

## Troubleshooting

### PostgreSQL recusando conexao

Verifique se o servico do PostgreSQL esta em execucao, se o banco `raizes_db` existe e se usuario/senha batem com `DB_USERNAME` e `DB_PASSWORD`.

### `.env` nao foi carregado

Este projeto nao usa biblioteca para carregar `.env` automaticamente. Configure variaveis no terminal, na IDE ou use os defaults de `application.properties`.

### Porta 8080 em uso

Encerre o processo que esta usando a porta ou configure outra porta com:

```powershell
$env:SERVER_PORT="8081"
.\gradlew.bat bootRun
```

### Erro `Unable to delete directory` no Windows

No Windows, VS Code, Red Hat Java ou outro processo Java pode segurar locks em arquivos dentro de `build/classes/java/main`. Se aparecer erro ao limpar ou recompilar:

1. Pare qualquer `bootRun` em execucao.
2. Feche o VS Code ou reinicie o Java Language Server.
3. Rode o comando novamente.

Isso e um lock local do ambiente, nao uma falha de configuracao da API.

## Observacoes de seguranca e LGPD

- Senhas sao armazenadas com BCrypt.
- Campos `senha` e hashes nao sao serializados nas responses de `Cliente` e `Funcionario`.
- A API usa JWT para autenticacao stateless.
- Endpoints protegidos exigem roles/perfis e pedidos validam propriedade para `CLIENTE`.
- O cadastro de cliente inclui consentimento LGPD.
- Dados pessoais sao coletados somente para identificacao, login, contato e operacao do pedido.
- Consentimento para campanhas/fidelidade deve poder ser revogado; a retencao deve seguir finalidade contratual e obrigacoes legais.
- Relatorios devem preferir dados anonimizados ou agregados e o acesso deve ser restrito por perfil.
- Acoes sensiveis do MVP registram auditoria.
- Segredos reais nao devem ser versionados no repositorio.

| Dado | Finalidade | Base legal | Retencao | Tratamento |
|---|---|---|---|---|
| Nome | Identificacao do cliente | Execucao de contrato | Enquanto o cadastro estiver ativo | Acesso restrito |
| E-mail | Login e comunicacao operacional | Execucao de contrato | Enquanto o cadastro estiver ativo | Unico, protegido e nao exposto com senha |
| Telefone | Contato sobre pedido | Execucao de contrato | Enquanto necessario | Acesso restrito |
| Consentimento | Campanhas e fidelidade | Consentimento | Ate revogacao | Data registrada |
| Historico de pedidos | Operacao e obrigacoes legais | Contrato/obrigacao legal | Prazo definido pela empresa | Acesso por perfil e uso agregado em relatorios |

## Promocoes e campanhas

Promocoes e campanhas foram modeladas como evolucao planejada e nao fazem
parte do MVP executavel. A evolucao deve considerar periodo de vigencia,
unidade, produto ou categoria, desconto percentual ou fixo, prioridade de
aplicacao, limite para que o desconto nao supere o total, auditoria de criacao
e aplicacao e consentimento para campanhas segmentadas.

## Evidencia de auditoria

Apos executar login, criacao de pedido e pagamento mock pela colecao Postman,
consulte o PostgreSQL:

```sql
SELECT id, acao, entidade, usuario, data_hora, valor_novo
FROM auditoria
ORDER BY id DESC;
```

Use a request/response e o resultado SQL na evidencia academica. Nao ha
endpoint publico para leitura da auditoria.

## Documentacao detalhada da API

Os contratos individuais dos endpoints estao documentados em [`DOCUMENTACAO_ENDPOINTS.md`](DOCUMENTACAO_ENDPOINTS.md) e no Swagger/OpenAPI.
