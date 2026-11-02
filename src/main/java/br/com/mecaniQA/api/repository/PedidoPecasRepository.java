package br.com.mecaniQA.api.repository;

import br.com.mecaniQA.api.jdbc.AbstractJdbcRepository;
import br.com.mecaniQA.api.jdbc.JdbcConnectionFactory;
import br.com.mecaniQA.api.jdbc.mapper.ItemPedidoPecaResultSetMapper;
import br.com.mecaniQA.api.jdbc.mapper.PedidoPecasResultSetMapper;
import br.com.mecaniQA.api.model.ItemPedidoPeca;
import br.com.mecaniQA.api.model.PedidoPecas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Repository
public class PedidoPecasRepository extends AbstractJdbcRepository<PedidoPecas> {
    public PedidoPecasRepository(JdbcConnectionFactory conexoes) {
        super(conexoes);
    }

    @Override
    protected String tabela() {
        return "pedido_pecas";
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO pedido_pecas (status,data_criacao,data_ultima_atualizacao) VALUES"
                + " (?,?,?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE pedido_pecas SET"
                + " status=?,data_criacao=?,data_ultima_atualizacao=?,versao=versao+1 WHERE"
                + " codigo=? AND versao=?";
    }

    @Override
    protected void bindInsert(PreparedStatement statement, PedidoPecas entidade)
            throws SQLException {
        statement.setString(1, entidade.getStatus().name());
        statement.setTimestamp(2, Timestamp.valueOf(entidade.getDataCriacao()));
        statement.setTimestamp(3, Timestamp.valueOf(entidade.getDataUltimaAtualizacao()));
    }

    @Override
    protected void bindUpdate(PreparedStatement statement, PedidoPecas entidade)
            throws SQLException {
        bindInsert(statement, entidade);
        statement.setLong(4, entidade.getCodigo());
        statement.setLong(5, entidade.getVersao());
    }

    @Override
    protected void salvarAssociacoes(Connection conexao, Long codigo, PedidoPecas entidade)
            throws SQLException {
        try (PreparedStatement statement =
                conexao.prepareStatement("DELETE FROM pedido_peca WHERE pedido_codigo=?")) {
            statement.setLong(1, codigo);
            statement.executeUpdate();
        }
        try (PreparedStatement statement =
                conexao.prepareStatement(
                        "INSERT INTO pedido_peca (pedido_codigo,peca_codigo,quantidade,posicao)"
                                + " VALUES (?,?,?,?)")) {
            for (int i = 0; i < entidade.getItens().size(); i++) {
                var item = entidade.getItens().get(i);
                statement.setLong(1, codigo);
                statement.setLong(2, item.getCodigoPeca());
                statement.setInt(3, item.getQuantidade());
                statement.setInt(4, i);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    protected PedidoPecas map(ResultSet resultSet, Connection conexao) throws SQLException {
        List<ItemPedidoPeca> itens = new ArrayList<>();
        try (PreparedStatement statement =
                conexao.prepareStatement(
                        "SELECT * FROM pedido_peca WHERE pedido_codigo=? ORDER BY posicao")) {
            statement.setLong(1, resultSet.getLong("codigo"));
            try (ResultSet linhas = statement.executeQuery()) {
                while (linhas.next()) {
                    itens.add(ItemPedidoPecaResultSetMapper.map(linhas));
                }
            }
        }
        return PedidoPecasResultSetMapper.map(resultSet, itens);
    }
}
