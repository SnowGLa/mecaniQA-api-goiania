package br.com.mecaniQA.api.jdbc;

import br.com.mecaniQA.api.exception.ConflitoDadosException;
import br.com.mecaniQA.api.exception.DadosInvalidosException;
import br.com.mecaniQA.api.exception.EntidadeNaoEncontradaException;
import br.com.mecaniQA.api.exception.PersistenciaException;
import br.com.mecaniQA.api.model.EntidadePersistente;
import br.com.mecaniQA.api.repository.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractJdbcRepository<T extends EntidadePersistente>
        implements Repository<T, Long> {
    private final JdbcConnectionFactory conexoes;

    protected AbstractJdbcRepository(JdbcConnectionFactory conexoes) {
        this.conexoes = conexoes;
    }

    protected abstract String tabela();

    protected abstract String insertSql();

    protected abstract String updateSql();

    protected abstract void bindInsert(PreparedStatement statement, T entidade) throws SQLException;

    protected abstract void bindUpdate(PreparedStatement statement, T entidade) throws SQLException;

    protected abstract T map(ResultSet resultSet, Connection conexao) throws SQLException;

    protected void salvarAssociacoes(Connection conexao, Long codigo, T entidade)
            throws SQLException {}

    @FunctionalInterface
    protected interface Trabalho<R> {
        R executar(Connection conexao) throws SQLException;
    }

    protected final <R> R transacao(Trabalho<R> trabalho) {
        try (Connection conexao = conexoes.abrir()) {
            conexao.setAutoCommit(false);
            try {
                R resultado = trabalho.executar(conexao);
                conexao.commit();
                return resultado;
            } catch (SQLException | RuntimeException excecao) {
                try {
                    conexao.rollback();
                } catch (SQLException rollback) {
                    excecao.addSuppressed(rollback);
                }
                throw excecao;
            }
        } catch (SQLException excecao) {
            throw traduzir(excecao);
        }
    }

    private RuntimeException traduzir(SQLException excecao) {
        if (excecao.getSQLState() != null && excecao.getSQLState().startsWith("23")) {
            return new ConflitoDadosException(
                    "A operacao conflita com dados relacionados.", excecao);
        }
        return new PersistenciaException("Falha ao acessar os dados.", excecao);
    }

    @Override
    public final T save(T entidade) {
        if (entidade.getCodigo() != null) {
            throw new DadosInvalidosException("Cadastro exige entidade nova.");
        }
        return transacao(
                conexao -> {
                    long codigo;
                    try (PreparedStatement statement =
                            conexao.prepareStatement(
                                    insertSql(), Statement.RETURN_GENERATED_KEYS)) {
                        bindInsert(statement, entidade);
                        if (statement.executeUpdate() != 1) {
                            throw new SQLException("Cadastro sem linha afetada.");
                        }
                        try (ResultSet chaves = statement.getGeneratedKeys()) {
                            if (!chaves.next()) {
                                throw new SQLException("Chave gerada ausente.");
                            }
                            codigo = chaves.getLong(1);
                            if (codigo <= 0) {
                                throw new SQLException("Chave gerada invalida.");
                            }
                        }
                    }
                    salvarAssociacoes(conexao, codigo, entidade);
                    return carregar(conexao, codigo)
                            .orElseThrow(
                                    () -> new PersistenciaException("Cadastro nao recuperado."));
                });
    }

    @Override
    public final T update(T entidade) {
        if (entidade.getCodigo() == null) {
            throw new DadosInvalidosException("Atualizacao exige codigo.");
        }
        return transacao(
                conexao -> {
                    try (PreparedStatement statement = conexao.prepareStatement(updateSql())) {
                        bindUpdate(statement, entidade);
                        if (statement.executeUpdate() != 1) {
                            if (carregar(conexao, entidade.getCodigo()).isEmpty()) {
                                throw new EntidadeNaoEncontradaException(
                                        "Registro nao encontrado.");
                            }
                            throw new ConflitoDadosException(
                                    "Registro alterado por outra requisicao; consulte novamente.");
                        }
                    }
                    salvarAssociacoes(conexao, entidade.getCodigo(), entidade);
                    return carregar(conexao, entidade.getCodigo())
                            .orElseThrow(
                                    () ->
                                            new EntidadeNaoEncontradaException(
                                                    "Registro nao encontrado."));
                });
    }

    protected final Optional<T> carregar(Connection conexao, Long codigo) throws SQLException {
        try (PreparedStatement statement =
                conexao.prepareStatement("SELECT * FROM " + tabela() + " WHERE codigo=?")) {
            statement.setLong(1, codigo);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet, conexao)) : Optional.empty();
            }
        }
    }

    @Override
    public final Optional<T> findById(Long id) {
        return transacao(conexao -> carregar(conexao, id));
    }

    @Override
    public final List<T> findAll() {
        return transacao(
                conexao -> {
                    try (PreparedStatement statement =
                                    conexao.prepareStatement(
                                            "SELECT * FROM " + tabela() + " ORDER BY codigo");
                            ResultSet resultSet = statement.executeQuery()) {
                        List<T> lista = new ArrayList<>();
                        while (resultSet.next()) {
                            lista.add(map(resultSet, conexao));
                        }
                        return List.copyOf(lista);
                    }
                });
    }

    @Override
    public final boolean deleteById(Long id) {
        return transacao(
                conexao -> {
                    try (PreparedStatement statement =
                            conexao.prepareStatement(
                                    "DELETE FROM " + tabela() + " WHERE codigo=?")) {
                        statement.setLong(1, id);
                        return statement.executeUpdate() == 1;
                    }
                });
    }
}
