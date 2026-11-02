-- MySQL 8.0.16+ / InnoDB. SOMENTE desenvolvimento: recria as sete tabelas
-- e remove seus dados. Execute explicitamente em um banco de desenvolvimento.
-- Nao contem usuarios, senhas ou dados pessoais.
CREATE DATABASE IF NOT EXISTS mecaniqa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mecaniqa;
DROP TABLE IF EXISTS pedido_peca;
DROP TABLE IF EXISTS ordem_servico_peca;
DROP TABLE IF EXISTS ordem_servico_servico;
DROP TABLE IF EXISTS pedido_pecas;
DROP TABLE IF EXISTS ordem_servico;
DROP TABLE IF EXISTS servico;
DROP TABLE IF EXISTS peca;

-- Catalogo de pecas. Somente tamanho e cor sao opcionais.
CREATE TABLE peca (
 codigo BIGINT PRIMARY KEY AUTO_INCREMENT,
 nome VARCHAR(255) NOT NULL, codigo_barras VARCHAR(255) NOT NULL, fornecedor_marca VARCHAR(255) NOT NULL,
 categoria VARCHAR(32) NOT NULL CHECK (categoria IN ('MOTOR','SUSPENSAO','FREIOS','ELETRICA','ACESSORIOS')),
 quantidade_estoque INT NOT NULL DEFAULT 0 CHECK (quantidade_estoque >= 0),
 preco_custo DECIMAL(14,2) NOT NULL CHECK (preco_custo >= 0),
 preco_venda DECIMAL(14,2) NOT NULL CHECK (preco_venda >= 0), tamanho VARCHAR(100), cor VARCHAR(100),
 data_cadastro DATETIME(6) NOT NULL, data_ultima_atualizacao DATETIME(6) NOT NULL,
 versao BIGINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Catalogo de servicos.
CREATE TABLE servico (
 codigo BIGINT PRIMARY KEY AUTO_INCREMENT, nome VARCHAR(255) NOT NULL,
 tempo_estimado_minutos INT NOT NULL DEFAULT 0 CHECK (tempo_estimado_minutos >= 0),
 custo_tabelado DECIMAL(14,2) NOT NULL CHECK (custo_tabelado >= 0), data_criacao DATETIME(6) NOT NULL,
 data_ultima_atualizacao DATETIME(6) NOT NULL, versao BIGINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Ordem e suas duas associacoes. Posicao preserva a ordem das listas JSON.
CREATE TABLE ordem_servico (
 codigo BIGINT PRIMARY KEY AUTO_INCREMENT,
 status VARCHAR(32) NOT NULL CHECK (status IN ('ABERTO','PENDENTE_PAGAMENTO','PAGO','EM_EXECUCAO','EXECUTADO')),
 data_criacao DATETIME(6) NOT NULL, data_ultima_atualizacao DATETIME(6) NOT NULL,
 versao BIGINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE ordem_servico_servico (
 codigo BIGINT PRIMARY KEY AUTO_INCREMENT, ordem_codigo BIGINT NOT NULL, servico_codigo BIGINT NOT NULL,
 posicao INT NOT NULL CHECK (posicao >= 0),
 UNIQUE KEY uq_os_servico (ordem_codigo,servico_codigo), UNIQUE KEY uq_os_servico_pos (ordem_codigo,posicao),
 KEY ix_os_servico_servico (servico_codigo),
 FOREIGN KEY (ordem_codigo) REFERENCES ordem_servico(codigo) ON DELETE CASCADE,
 FOREIGN KEY (servico_codigo) REFERENCES servico(codigo) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE ordem_servico_peca (
 codigo BIGINT PRIMARY KEY AUTO_INCREMENT, ordem_codigo BIGINT NOT NULL, peca_codigo BIGINT NOT NULL,
 posicao INT NOT NULL CHECK (posicao >= 0),
 UNIQUE KEY uq_os_peca (ordem_codigo,peca_codigo), UNIQUE KEY uq_os_peca_pos (ordem_codigo,posicao),
 KEY ix_os_peca_peca (peca_codigo),
 FOREIGN KEY (ordem_codigo) REFERENCES ordem_servico(codigo) ON DELETE CASCADE,
 FOREIGN KEY (peca_codigo) REFERENCES peca(codigo) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Pedido e entidade associativa com quantidade. Versao detecta atualizacao concorrente.
CREATE TABLE pedido_pecas (
 codigo BIGINT PRIMARY KEY AUTO_INCREMENT,
 status VARCHAR(32) NOT NULL CHECK (status IN ('ORCANDO','PENDENTE_PAGAMENTO','PAGO_FATURADO','ENTREGUE')),
 data_criacao DATETIME(6) NOT NULL, data_ultima_atualizacao DATETIME(6) NOT NULL,
 versao BIGINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE pedido_peca (
 codigo BIGINT PRIMARY KEY AUTO_INCREMENT, pedido_codigo BIGINT NOT NULL, peca_codigo BIGINT NOT NULL,
 quantidade INT NOT NULL CHECK (quantidade > 0), posicao INT NOT NULL CHECK (posicao >= 0),
 UNIQUE KEY uq_pedido_peca (pedido_codigo,peca_codigo), UNIQUE KEY uq_pedido_pos (pedido_codigo,posicao),
 KEY ix_pedido_peca (peca_codigo),
 FOREIGN KEY (pedido_codigo) REFERENCES pedido_pecas(codigo) ON DELETE CASCADE,
 FOREIGN KEY (peca_codigo) REFERENCES peca(codigo) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
