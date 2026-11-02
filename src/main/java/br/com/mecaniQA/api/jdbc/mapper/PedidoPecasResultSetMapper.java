package br.com.mecaniQA.api.jdbc.mapper;

import br.com.mecaniQA.api.model.ItemPedidoPeca;
import br.com.mecaniQA.api.model.PedidoPecas;
import br.com.mecaniQA.api.model.StatusPedidoPecas;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class PedidoPecasResultSetMapper {
    private PedidoPecasResultSetMapper() {}

    public static PedidoPecas map(ResultSet resultSet, List<ItemPedidoPeca> itens)
            throws SQLException {
        return PedidoPecas.reconstituir(
                resultSet.getLong("codigo"),
                JdbcValues.enumeracao(resultSet, "status", StatusPedidoPecas.class),
                itens,
                JdbcValues.data(resultSet, "data_criacao"),
                JdbcValues.data(resultSet, "data_ultima_atualizacao"),
                resultSet.getLong("versao"));
    }
}
