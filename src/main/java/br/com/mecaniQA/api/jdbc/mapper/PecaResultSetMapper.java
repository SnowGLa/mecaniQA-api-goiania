package br.com.mecaniQA.api.jdbc.mapper;

import br.com.mecaniQA.api.model.CategoriaPeca;
import br.com.mecaniQA.api.model.Peca;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public final class PecaResultSetMapper {
    private PecaResultSetMapper() {}

    public static Peca map(ResultSet resultSet) throws SQLException {
        return Peca.reconstituir(
                resultSet.getLong("codigo"),
                resultSet.getString("nome"),
                resultSet.getString("codigo_barras"),
                resultSet.getString("fornecedor_marca"),
                JdbcValues.enumeracao(resultSet, "categoria", CategoriaPeca.class),
                resultSet.getInt("quantidade_estoque"),
                JdbcValues.moeda(resultSet, "preco_custo"),
                JdbcValues.moeda(resultSet, "preco_venda"),
                resultSet.getString("tamanho"),
                resultSet.getString("cor"),
                JdbcValues.data(resultSet, "data_cadastro"),
                JdbcValues.data(resultSet, "data_ultima_atualizacao"),
                resultSet.getLong("versao"));
    }

    public static void bind(PreparedStatement statement, Peca entidade) throws SQLException {
        statement.setString(1, entidade.getNome());
        statement.setString(2, entidade.getCodigoBarras());
        statement.setString(3, entidade.getFornecedorMarca());
        statement.setString(4, entidade.getCategoria().name());
        statement.setInt(5, entidade.getQuantidadeEstoque());
        JdbcValues.moeda(statement, 6, entidade.getPrecoCusto());
        JdbcValues.moeda(statement, 7, entidade.getPrecoVenda());
        statement.setString(8, entidade.getTamanho());
        statement.setString(9, entidade.getCor());
        statement.setTimestamp(10, Timestamp.valueOf(entidade.getDataCadastro()));
        statement.setTimestamp(11, Timestamp.valueOf(entidade.getDataUltimaAtualizacao()));
    }
}
