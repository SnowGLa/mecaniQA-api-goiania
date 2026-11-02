# OAT 2

Implementação local sobre o ponto de restauração `29250fb`, em Java 21 e Spring Boot.
Os modelos, repositories e 13 testes aprovados da OAT 1 foram mantidos. Os controllers
de peças e serviços agora recebem DTOs e devolvem DTOs, mantendo o contrato JSON e
os campos que o PUT já atualizava na OAT 1.

## Modelagem

- Códigos por tipo usam `AtomicLong`; datas usam `LocalDateTime`, como na OAT 1.
  Dados e contadores vivem somente na memória do processo.
- `OrdemServico.builder()` é a única construção pública de ordens. O construtor
  é privado. O Builder oferece `codigosServicos(...)`, `codigosPecas(...)` e
  `status(...)`. As listas começam vazias; é obrigatório informar ao menos um
  serviço ou uma peça. Cada código deve ser positivo, não nulo, sem repetição
  na sua coleção e existente no Singleton correspondente. As duas listas são
  copiadas defensivamente e mantidas imutáveis no Builder e na entidade.
  Não há setters públicos na OS.
- O Builder usa `ABERTO` por padrão e permite status explícito para construção
  interna. Uma chamada explícita a `status(null)` é rejeitada por `build()`.
  A API continua criando ordens somente em `ABERTO`, pois o Mapper não aceita
  status do DTO de cadastro. Depois da criação, avança somente nesta sequência:
  `ABERTO -> PENDENTE_PAGAMENTO -> PAGO -> EM_EXECUCAO -> EXECUTADO`.
- O status do pedido nasce `ORCANDO` e avança somente nesta sequência:
  `ORCANDO -> PENDENTE_PAGAMENTO -> PAGO_FATURADO -> ENTREGUE`.
- Repetir o status, retroceder, pular etapas ou informar um status desconhecido
  retorna 400, sem modificar o objeto ou sua data. Alterações válidas atualizam
  `dataUltimaAtualizacao` e preservam `dataCriacao`.
- Um pedido exige o campo `itens`. `{"itens":[]}` inicia um orçamento vazio.
  Também é possível informar vários itens no cadastro. Cada item é imutável,
  referencia uma peça existente e exige quantidade inteira maior que zero.
  Itens repetidos são somados tanto no cadastro quanto na inclusão posterior;
  estouro de `int` retorna 400 e preserva o estado anterior.
- Listas de entrada são copiadas defensivamente. Listas das entidades, dos DTOs
  de saída e dos repositories são imutáveis. Itens não têm setters; uma soma
  substitui o item, preservando snapshots anteriores.
- Os novos repositories seguem o Singleton da OAT 1: construtor privado,
  `getInstance()` sincronizado e armazenamento em listas. `salvar()` cadastra ou
  substitui pelo código, sem duplicar o registro. Atualizações usam as operações
  de domínio e `salvar()`. Os métodos de acesso dos novos repositories são
  sincronizados; isso não transforma toda a API em uma transação concorrente.
- A existência das referências é verificada na criação do vínculo, usando os
  Singletons existentes, inclusive na construção direta por Builder ou item.
  As rotas de exclusão da OAT 1 mantêm seu comportamento e não apagam vínculos
  já registrados nas ordens ou pedidos.
- DTOs de entrada e saída são records separados. Os campos gerados (código,
  datas e status inicial) não fazem parte dos DTOs de cadastro. Campos extras
  são ignorados, preservando os testes JSON aprovados da OAT 1. DTOs específicos
  de alteração de status recebem apenas `status`; a interpretação e as respostas
  400/404 são feitas manualmente nos controllers. Os Mappers são classes com
  métodos estáticos. JSON malformado ou de tipo incompatível recebe o 400 padrão
  do Spring MVC durante a leitura do corpo.
- O cadastro de OS aceita `codigosServicos`, `codigosPecas` ou ambos. No DTO,
  coleções ausentes ou nulas são normalizadas para listas vazias; elementos
  nulos continuam inválidos. Requisições antigas contendo somente
  `codigosServicos` permanecem válidas. No Builder, fornecer explicitamente
  uma coleção nula é inválido. Os construtores anteriores dos DTOs de OS
  foram mantidos como sobrecargas, sem remover testes existentes.
- Todos os métodos com `@RequestBody` verificam DTO nulo antes de acessar campos
  ou consultar recursos. JSON literal `null` retorna 400, inclusive em PUT e
  inclusão de itens. O Spring MVC também pode rejeitá-lo antes do Controller.

## Rotas

| Método | Rota | Resultado |
| --- | --- | --- |
| POST | `/api/ordens-servico` | 201; 400 para cadastro inválido |
| GET | `/api/ordens-servico` | 200 |
| GET | `/api/ordens-servico/{codigo}` | 200 ou 404 |
| PUT | `/api/ordens-servico/{codigo}/status` | 200, 400 ou 404 |
| POST | `/api/pedidos-pecas` | 201; 400 para cadastro inválido |
| GET | `/api/pedidos-pecas` | 200 |
| GET | `/api/pedidos-pecas/{codigo}` | 200 ou 404 |
| POST | `/api/pedidos-pecas/{codigo}/itens` | 200; 400 para código/quantidade inválidos; 404 para pedido ou peça inexistente |
| PUT | `/api/pedidos-pecas/{codigo}/status` | 200, 400 ou 404 |

Mantido **POST** para inclusão de itens: o material de OAT 1 disponível no
projeto não especifica um verbo alternativo para essa operação. Os endpoints
anteriores `/api/pecas` e `/api/servicos` preservam POST, GET, PUT e DELETE.

Exemplos de corpos (substitua os códigos pelos retornados nos cadastros):

```json
{"codigosServicos":[1,2]}
```

```json
{"codigosPecas":[1,2]}
```

```json
{"codigosServicos":[1],"codigosPecas":[2]}
```

```json
{"itens":[{"codigoPeca":1,"quantidade":2},{"codigoPeca":2,"quantidade":4}]}
```

```json
{"codigoPeca":1,"quantidade":3}
```

```json
{"status":"PENDENTE_PAGAMENTO"}
```

## Testes e revisão

Os testes novos usam MockMvc sem injeção para exercitar binding JSON, DTOs,
controllers e Mappers em conjunto. Os 13 testes da OAT 1 permanecem inalterados,
incluindo os testes com servidor HTTP real. Há testes de domínio para Builder,
códigos concorrentes, datas, matrizes de transição, itens, overflow e coleções;
testes dos novos Singletons e de cadastro/atualização; e testes dos contratos DTO.

A revisão 2 adiciona `Oat2Revisao2Tests.java`, preservando os 66 testes da primeira
revisão: OS apenas com peças, apenas com serviços e mista; listas vazias ou com
referências inválidas; status padrão e explícito do Builder; cópias imutáveis;
proteção de status/códigos/datas no JSON; literal `null` nos nove endpoints com
corpo; e chamadas diretas aos Controllers com DTO nulo.

## Diagramas e Postman

- `docs/diagramas/oat2-diagrama-classes.puml`: classes de domínio, Builder aninhado,
  ItemPedidoPeca como entidade associativa, enums, Singletons, DTOs, Mappers,
  Controllers, relacionamentos e multiplicidades. Listas de códigos representam
  referências em memória, não vínculos de persistência.
- `docs/diagramas/oat2-diagrama-atividade.puml`: visão do Controller para
  `POST /api/pedidos-pecas/{codigo}/itens`, incluindo validações, 400, 404,
  inclusão/soma e resposta 200.
- `postman/mecaniQA-api-oat2-goiania.postman_collection.json`: collection v2.1
  com 68 requisições de sucesso e erro para todas as rotas da OAT 1 e OAT 2.
  A variável `baseUrl` começa em `http://localhost:8080`. Execute as pastas em
  ordem; os cadastros guardam os códigos em variáveis da collection. Cada
  requisição possui teste do status esperado. A última pasta exclui somente
  as peças e serviços criados pelos exemplos; ordens e pedidos continuam em
  memória. Após reiniciar a API, execute novamente desde a primeira pasta.

Os fontes PlantUML podem ser renderizados com PlantUML e Graphviz. A apresentação
em PDF fica fora desta revisão.

## Arquivos

Alterados em `src/main/java/br/com/mecaniQA/api/controller/`:
`PecaController.java` e `ServicoController.java`.

Criados em `src/main/java/br/com/mecaniQA/api/`:

- `controller/`: `OrdemServicoController.java`, `PedidoPecasController.java`.
- `model/`: `OrdemServico.java`, `PedidoPecas.java`, `ItemPedidoPeca.java`,
  `StatusOrdemServico.java`, `StatusPedidoPecas.java`.
- `repository/`: `OrdemServicoRepository.java`, `PedidoPecasRepository.java`.
- `mapper/`: `PecaMapper.java`, `ServicoMapper.java`, `OrdemServicoMapper.java`,
  `PedidoPecasMapper.java`, `ItemPedidoPecaMapper.java`.
- `dto/`: `PecaRequest.java`, `PecaResponse.java`, `ServicoRequest.java`,
  `ServicoResponse.java`, `OrdemServicoRequest.java`, `OrdemServicoResponse.java`,
  `StatusOrdemServicoRequest.java`, `PedidoPecasRequest.java`, `PedidoPecasResponse.java`,
  `StatusPedidoPecasRequest.java`, `ItemPedidoPecaRequest.java`, `ItemPedidoPecaResponse.java`.

Criados em `src/test/java/br/com/mecaniQA/api/`:
`Oat2HttpTests.java`, `DtoContractTests.java`, `model/Oat2ModelTests.java`,
`repository/Oat2RepositoryTests.java`, `Oat2Revisao2Tests.java`.

Documentação criada: `docs/OAT2.md` (este arquivo).

Artefatos adicionais: os dois fontes em `docs/diagramas/` e a collection em
`postman/`, listados acima.

Arquivos ajustados especificamente na revisão 2: os quatro Controllers,
`model/OrdemServico.java`, `dto/OrdemServicoRequest.java`,
`dto/OrdemServicoResponse.java`, `mapper/OrdemServicoMapper.java` e esta documentação.
Os quatro arquivos novos da revisão 2 são `Oat2Revisao2Tests.java`, os dois
diagramas e a collection Postman.

## Verificação local

Executar com Java 21:

```powershell
.\gradlew.bat test --console=plain
git diff --check
git status --short --branch
```

O diff desta revisão fica em `build/implementacao-oat2-revisao2.diff`, comparado a `29250fb`.
Ele inclui também os arquivos novos ainda não rastreados, sem exigir staging
ou commit. A implementação permanece local para revisão.
