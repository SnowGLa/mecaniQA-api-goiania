package br.com.mecaniQA.api.repository;

import br.com.mecaniQA.api.jdbc.AbstractJdbcRepository;
import br.com.mecaniQA.api.jdbc.JdbcConnectionFactory;
import br.com.mecaniQA.api.jdbc.mapper.OrdemServicoResultSetMapper;
import br.com.mecaniQA.api.model.OrdemServico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Repository
public class OrdemServicoRepository extends AbstractJdbcRepository<OrdemServico> {
    public OrdemServicoRepository(JdbcConnectionFactory conexoes) {
        super(conexoes);
    }

    @Override
    protected String tabela() {
        return "ordem_servico";
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO ordem_servico (status,data_criacao,data_ultima_atualizacao) VALUES"
                + " (?,?,?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE ordem_servico SET"
                + " status=?,data_criacao=?,data_ultima_atualizacao=?,versao=versao+1 WHERE"
                + " codigo=? AND versao=?";
    }

    @Override
    protected void bindInsert(PreparedStatement statement, OrdemServico entidade)
            throws SQLException {
        statement.setString(1, entidade.getStatus().name());
        statement.setTimestamp(2, Timestamp.valueOf(entidade.getDataCriacao()));
        statement.setTimestamp(3, Timestamp.valueOf(entidade.getDataUltimaAtualizacao()));
    }

    @Override
    protected void bindUpdate(PreparedStatement statement, OrdemServico entidade)
            throws SQLException {
        bindInsert(statement, entidade);
        statement.setLong(4, entidade.getCodigo());
        statement.setLong(5, entidade.getVersao());
    }

    private List<Long> referencias(Connection conexao, Long codigo, String tabela, String coluna)
            throws SQLException {
        try (PreparedStatement statement =
                conexao.prepareStatement(
                        "SELECT "
                                + coluna
                                + " FROM "
                                + tabela
                                + " WHERE ordem_codigo=? ORDER BY posicao")) {
            statement.setLong(1, codigo);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Long> lista = new ArrayList<>();
                while (resultSet.next()) {
                    lista.add(resultSet.getLong(1));
                }
                return List.copyOf(lista);
            }
        }
    }

    private void gravar(
            Connection conexao, Long codigo, String tabela, String coluna, List<Long> valores)
            throws SQLException {
        try (PreparedStatement apagar =
                conexao.prepareStatement("DELETE FROM " + tabela + " WHERE ordem_codigo=?")) {
            apagar.setLong(1, codigo);
            apagar.executeUpdate();
        }
        try (PreparedStatement inserir =
                conexao.prepareStatement(
                        "INSERT INTO "
                                + tabela
                                + " (ordem_codigo,"
                                + coluna
                                + ",posicao) VALUES (?,?,?)")) {
            for (int i = 0; i < valores.size(); i++) {
                inserir.setLong(1, codigo);
                inserir.setLong(2, valores.get(i));
                inserir.setInt(3, i);
                inserir.addBatch();
            }
            inserir.executeBatch();
        }
    }

    @Override
    protected void salvarAssociacoes(Connection conexao, Long codigo, OrdemServico entidade)
            throws SQLException {
        gravar(
                conexao,
                codigo,
                "ordem_servico_servico",
                "servico_codigo",
                entidade.getCodigosServicos());
        gravar(conexao, codigo, "ordem_servico_peca", "peca_codigo", entidade.getCodigosPecas());
    }

    @Override
    protected OrdemServico map(ResultSet resultSet, Connection conexao) throws SQLException {
        Long codigo = resultSet.getLong("codigo");
        return OrdemServicoResultSetMapper.map(
                resultSet,
                referencias(conexao, codigo, "ordem_servico_servico", "servico_codigo"),
                referencias(conexao, codigo, "ordem_servico_peca", "peca_codigo"));
    }
}
