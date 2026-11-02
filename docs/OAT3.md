# OAT 3 — JDBC e MySQL

Implementação local baseada no commit aprovado
`8a68b07` da OAT 2. A `main` não foi alterada. Não foi feita apresentação PDF.

## Ambiente encontrado

A validação final utiliza **MySQL Community Server 8.0.46** e **JDK 21
(Eclipse Temurin 21.0.12.1)**. O schema `mecaniqa` foi criado previamente com
as sete tabelas de `script_mecaniqa.sql`: `peca`, `servico`, `ordem_servico`,
`ordem_servico_servico`, `ordem_servico_peca`, `pedido_pecas` e `pedido_peca`.

Os XMLs de `build/test-results/mysqlIntegrationTest` confirmam **13 testes
executados, 13 sucessos, zero falhas, zero erros e zero skipped**. A execução
da tarefa foi concluída com `BUILD SUCCESSFUL` e código final 0; a integração
não foi reexecutada na conferência final dos resultados. Nenhuma credencial
é registrada nesta documentação.

## Arquitetura implementada

`Controller → Service → Repository → AbstractJdbcRepository → JDBC → MySQL`

- Os quatro Controllers recebem seus Services pelo único construtor. Seus
  contratos HTTP continuam usando os mesmos DTOs e Mappers da OAT 2.
- `PecaService`, `ServicoService`, `OrdemServicoService` e `PedidoPecasService`
  são componentes `@Service`. Validam conteúdo, referências, transições,
  quantidades, overflow e operações CRUD. A categoria continua obrigatória.
  Referências inválidas no cadastro de OS/pedido retornam 400; peça inexistente
  ao adicionar item a pedido existente retorna 404, como na OAT 2.
- `Repository<T, ID>` define exatamente `save`, `findById`, `findAll`, `update`
  e `deleteById`. Os quatro repositories concretos implementam esse contrato
  pela herança de `AbstractJdbcRepository<T>`, especializado em IDs `Long`.
  São componentes Spring, sem instância estática ou listas de armazenamento.
- `CrudService<T>` compartilha busca, listagem e exclusão. Recebe a interface
  genérica, permitindo doubles exclusivamente em testes. Na aplicação, Spring
  resolve os parâmetros genéricos para os repositories JDBC correspondentes.
- `JdbcConnectionFactory` recebe propriedades pelo construtor e usa
  `DriverManager.getConnection`. Cada chamada abre outra conexão. Não existe
  conexão global, pool próprio ou conexão aberta no startup.
- A única dependência adicionada é `com.mysql:mysql-connector-j`, em runtime,
  com versão gerenciada pelo Spring Boot. Não existe banco alternativo em memória.

## Template Method e ResultSet Mappers

As cinco operações CRUD da classe abstrata são `final`. O esqueleto abre
conexão, desliga autocommit, prepara/executa statements, chama os hooks da
entidade, confirma a transação e fecha os recursos. Em falha, tenta rollback
antes do fechamento; falha adicional de rollback é preservada como suppressed.
Todas as consultas e alterações utilizam try-with-resources.

Os hooks concretos fornecem tabela/SQL constante, parâmetros, conversão de
linha e gravação das associações. Identificadores SQL não vêm do cliente;
valores externos são ligados com PreparedStatement. O código SQL é manual.

`PecaResultSetMapper`, `ServicoResultSetMapper`, `OrdemServicoResultSetMapper`,
`PedidoPecasResultSetMapper` e `ItemPedidoPecaResultSetMapper` ficam em
`jdbc/mapper`. `JdbcValues` converte Timestamp em LocalDateTime, DECIMAL em
Double para manter o DTO anterior e textos em enums. Datas obrigatórias ou
enums persistidos inválidos geram falha de persistência, sem vazar detalhes.
Os Mappers de DTO continuam em `mapper`; Controllers e Services não recebem
ResultSet nem executam SQL.

## Identidade, reconstituição e transações

Entidades novas têm código nulo até serem cadastradas. `save` usa
`RETURN_GENERATED_KEYS`, obtém uma chave positiva e recarrega o agregado na
mesma transação. A instância retornada contém o ID real e as datas persistidas;
a instância de entrada não ganha um ID simulado. Factories `reconstituir`
preservam ID, status, datas, referências e versão do banco, sem setters de
código ou datas. Não há contadores de identidade em produção.

O Builder de OS continua privado/aninhado, com `status`, `codigosServicos` e
`codigosPecas`. Valida estrutura e mantém cópias imutáveis; o Service verifica
existência das referências. A API sempre cria OS em `ABERTO`; pedidos nascem
`ORCANDO`. Transições sequenciais são verificadas pelos Services. A data de
atualização muda quando a entidade é alterada, e a data original é preservada.

O cadastro/atualização de OS grava a linha principal e ambas as associações
na mesma conexão/transação. O pedido grava pai e todos os itens do mesmo modo.
Ao atualizar um agregado, as associações são substituídas dentro da transação.
Uma falha em qualquer item desfaz também a alteração do pai. Listas são
carregadas nas buscas e listagens, ordenadas pela coluna `posicao`.

O Service consolida peças repetidas com `Math.addExact`. Overflow retorna 400
antes de persistir. Uma coluna interna `versao` implementa atualização otimista:
o UPDATE exige a versão lida e incrementa-a. Caso outro cliente já tenha
alterado o registro, retorna 409, evitando sobrescrita silenciosa de quantidade
ou status. O cliente deve reler e repetir a operação se ainda for necessária.
Esse mecanismo não garante que duas adições concorrentes sejam ambas aceitas.

As listagens devolvem cópias imutáveis. O campo interno `versao` não é exposto
nos DTOs. Datas usam DATETIME(6); a leitura após a gravação reflete a precisão
efetiva do MySQL. Valores monetários usam DECIMAL(14,2) no banco e mantêm Double
no contrato HTTP anterior; o banco armazena duas casas decimais.

## Script e configuração

`script_mecaniqa.sql`, na raiz, contém sete tabelas InnoDB/utf8mb4:

| Tabela | Conteúdo |
| --- | --- |
| `peca` | Catálogo, categoria, estoque, preços e datas |
| `servico` | Catálogo, duração, custo e datas |
| `ordem_servico` | Identidade, status, datas e versão |
| `ordem_servico_servico` | Referências de serviços e posição na lista |
| `ordem_servico_peca` | Referências de peças e posição na lista |
| `pedido_pecas` | Identidade, status, datas e versão |
| `pedido_peca` | Associação com peça, quantidade positiva e posição |

Todas têm PK AUTO_INCREMENT. Há FKs indexadas, unicidade por par e por posição,
CHECKs de enum/quantidade e DECIMAL nos preços. Use MySQL **8.0.16 ou superior**
para enforcement dos CHECKs. Exclusão de OS/pedido remove suas associações por
CASCADE; exclusão de peça/serviço referenciado é impedida por RESTRICT e retorna
409. Essa proteção substitui a ausência de integridade referencial da OAT 2.

**O script é de reconstrução de desenvolvimento e apaga os dados das sete
tabelas de `mecaniqa`. Revise o destino antes de executá-lo.** Ele não roda no
startup nem nos testes. Com o cliente MySQL já instalado, abra-o e execute:

```sql
SOURCE C:/caminho/do/projeto/script_mecaniqa.sql;
```

Configure no terminal usado para iniciar a aplicação:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/mecaniqa?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USERNAME = 'usuario_local'
# Defina DB_PASSWORD no ambiente local, sem versionar seu valor.
.\gradlew.bat bootRun --console=plain
```

Defaults locais: essa URL, usuário `root` e senha vazia. Não há senha real no
repositório. Ajuste o usuário, permissões, senha e TLS da URL ao ambiente usado.
A aplicação inicia sem conectar; a primeira operação de dados exige o schema
e uma conexão válida. Não inicializa containers ou provisiona banco.

## Erros e rotas

Cada um dos 21 endpoints dos quatro Controllers contém seu próprio bloco
try/catch, com as quatro capturas explícitas e os retornos 400, 404, 409 e 500.
Não há classe utilitária que centralize a captura nem tratamento global.
Nove métodos verificam
DTO nulo antes de consultar recursos. JSON literal `null` e conteúdo inválido
continuam retornando 400. Erros de binding também recebem o 400 do Spring MVC.

| Exceção | HTTP | Caso |
| --- | --- | --- |
| `DadosInvalidosException` | 400 | Conteúdo, referência de cadastro, transição ou quantidade inválida |
| `EntidadeNaoEncontradaException` | 404 | ID não encontrado, inclusive peça ao adicionar item |
| `ConflitoDadosException` | 409 | SQLState de integridade ou versão desatualizada |
| `PersistenciaException` | 500 | Falha inesperada de conexão, SQL ou mapeamento persistido |

As respostas de erro são vazias: não devolvem SQL, URL, usuário, senha ou causa
interna. A causa é preservada na exceção para diagnóstico interno. Propriedades
de erro do servidor também impedem inclusão de mensagem e stacktrace.

Todas as rotas da OAT 2 foram preservadas. Foram acrescentadas:

- `DELETE /api/ordens-servico/{codigo}`;
- `DELETE /api/pedidos-pecas/{codigo}`.

As duas retornam 204 se removidas, 404 se ausentes, 409 para conflito quando
aplicável e 500 para falha de persistência. As demais continuam usando 200/201,
204, 400 e 404, agora também com tratamento de falhas 409/500.

## Testes e Postman

```powershell
.\gradlew.bat test --console=plain
```

Os testes comuns não exigem banco. Casos HTTP usam os Services reais e doubles
de Repository em `src/test/support`; o teste com servidor real importa uma
configuração de teste com esses doubles. Produção não contém implementação
alternativa em memória. Os antigos testes de Singleton/contador foram
substituídos por cobertura da interface, arquitetura, reconstituição, JDBC,
chaves geradas, transações e fechamento em sucesso/falha.

Os testes JDBC usam mocks de Connection/Statement/ResultSet, identificados como
doubles, e não demonstram compatibilidade real com MySQL. Os testes reais são
os **13 casos** de `integration/MysqlIntegrationTests.java`, separados por tag
`mysql`. Para executá-los, prepare previamente um schema MySQL descartável com
a mesma estrutura e configure explicitamente:

```powershell
$env:MYSQL_TEST_URL = 'jdbc:mysql://localhost:3306/mecaniqa?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:MYSQL_TEST_USERNAME = 'usuario_de_teste'
# Defina MYSQL_TEST_PASSWORD no ambiente; não copie credenciais para arquivos.
.\gradlew.bat mysqlIntegrationTest --console=plain
```

A tarefa só executa com as três variáveis presentes. Não executa DDL. Os casos
criam dados próprios e os apagam ao final, pais antes dos filhos. Testam CRUD,
associações, rollback real, restrições, versões, chaves entre instâncias,
credenciais inválidas e uma rota HTTP com JDBC real. Um erro de credenciais
não deve revelar detalhes na resposta.

A collection histórica `postman/mecaniQA-api-oat2-goiania.postman_collection.json`
foi restaurada byte a byte a partir de `8a68b07`, com 68 requisições.
A nova `postman/mecaniQA-api-oat3-goiania.postman_collection.json` contém
80 requisições, as novas exclusões e cenários 409. Os IDs dos exemplos são
capturados e os pais são removidos antes das peças/serviços. O caso 500 é
opcional, pulado por padrão (`executarFalhaBanco=false`); só habilite em uma
instância separada configurada para banco indisponível. Não desliga o banco.
Esses exemplos não foram executados contra MySQL neste ambiente.

Os diagramas `oat2-*` ficam preservados como registro da OAT 2; não representam
a nova arquitetura de persistência.

O diff completo contra `8a68b07` fica em `build/implementacao-oat3-revisao2.diff`,
incluindo arquivos novos sem exigir staging, commit ou push.

## Resultado desta execução

- Testes comuns: **122 executados, 122 sucessos, zero falhas, zero erros e
  zero skipped**, conforme os nove XMLs de `build/test-results/test`.
  A tarefa `test` foi executada com JDK 21 (Eclipse Temurin 21.0.12.1),
  com `--rerun-tasks` após a primeira chamada retornar `UP-TO-DATE`.
  Gradle concluiu com `BUILD SUCCESSFUL` em 12 s e código final 0.
- Integração real: **13 executados, 13 sucessos, zero falhas, zero erros e
  zero skipped**, conforme o XML de `build/test-results/mysqlIntegrationTest`,
  contra MySQL Community Server 8.0.46 e o schema preparado com as sete
  tabelas de `script_mecaniqa.sql`.
- As buscas de tecnologias proibidas, acesso a repository nos Controllers,
  SQL/java.sql nos Services e antigos Singletons/contadores ficaram vazias.
  Os imports java.sql de produção estão restritos a `jdbc/` (incluindo os
  Mappers) e `repository/`.
- As duas collections tiveram JSON, variáveis e sintaxe de scripts conferidos;
  a collection da OAT 3 também teve a cobertura das 21 rotas
  conferidos localmente, sem execução contra banco real.
- A execução real cobre os cenários de conexão, CRUD, associações, rollback,
  restrição referencial, conflito de versão, chaves geradas, valores/datas,
  credenciais inválidas e HTTP com JDBC definidos nos 13 testes de integração.

## Inventário das alterações

Alterados: `build.gradle.kts`, `src/main/resources/application.properties`, a
os quatro Controllers, os quatro repositories,
os cinco modelos persistidos, `ItemPedidoPecaMapper` e `PedidoPecasMapper`.
DTOs, enums, rotas prévias e os demais Mappers de DTO permaneceram com os
contratos da OAT 2.

Criados na produção, sob `src/main/java/br/com/mecaniQA/api/`:

- `exception/`: `DadosInvalidosException.java`, `EntidadeNaoEncontradaException.java`,
  `PersistenciaException.java`, `ConflitoDadosException.java`;
- `jdbc/`: `JdbcConnectionFactory.java`, `AbstractJdbcRepository.java`;
- `jdbc/mapper/`: `JdbcValues.java`, `PecaResultSetMapper.java`,
  `ServicoResultSetMapper.java`, `OrdemServicoResultSetMapper.java`,
  `PedidoPecasResultSetMapper.java`, `ItemPedidoPecaResultSetMapper.java`;
- `model/EntidadePersistente.java` e `repository/Repository.java`;
- `service/`: `PecaService.java`, `ServicoService.java`, `OrdemServicoService.java`,
  `PedidoPecasService.java`, `CrudService.java`, `ServiceRules.java`.

Criados fora da produção: `script_mecaniqa.sql`, `docs/OAT3.md`,
`postman/mecaniQA-api-oat3-goiania.postman_collection.json`,
e, sob `src/test/java/br/com/mecaniQA/api/`,
`ArchitectureServiceTests.java`, `jdbc/JdbcRepositoryTests.java`,
`jdbc/ResultSetMapperTests.java`, `integration/MysqlIntegrationTests.java`,
`support/MemoryRepository.java`, `support/TestRepositories.java` e
`support/TestRepositoryConfiguration.java`.

Testes adaptados: `JsonRequestTests`, `Oat2HttpTests`, `Oat2Revisao2Tests` e
`DataUltimaAtualizacaoTests`. Removidos os testes antigos de implementação em
memória `CodigoUnicoTests`, `Oat2ModelTests`, `Oat2RepositoryTests`,
`RepositoryListTests` e `RepositorySingletonTests`; sua cobertura pertinente
foi redistribuída para Services, reconstituição e JDBC, sem manter expectativas
de Singleton ou identidade simulada incompatíveis com a OAT 3.

## Ajustes da revisão 2

Os Controllers tratam as exceções em cada endpoint, mesmo com repetição, para
atender ao exercício acadêmico. Os testes arquiteturais analisam os métodos
Java e verificam a presença das quatro capturas em cada método, além da ausência
de uma classe centralizadora. Os cenários HTTP exercitam todos os endpoints com
400/404/409/500 e verificam corpos vazios; a cobertura foi incorporada aos testes
existentes, mantendo 122 testes comuns.

Os 46 arquivos Java criados ou alterados na OAT 3 foram reformatados com quatro
espaços, imports de tipos explícitos, blocos delimitados e instruções separadas.
Variáveis JDBC e parâmetros dos Services passaram a ter nomes descritivos.
As duas collections são arquivos distintos; a histórica não foi transformada.

A validação final confirma os 13 testes MySQL executados com sucesso, sem
casos ignorados, além dos 122 testes comuns aprovados com JDK 21. A integração
real deixou de estar pendente; a collection Postman permanece sem execução
contra o banco, conforme registrado acima.
