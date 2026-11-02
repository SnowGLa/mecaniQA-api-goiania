package br.com.mecaniQA.api.jdbc.mapper;

import br.com.mecaniQA.api.model.ItemPedidoPeca;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class ItemPedidoPecaResultSetMapper {
    private ItemPedidoPecaResultSetMapper() {}

    public static ItemPedidoPeca map(ResultSet resultSet) throws SQLException {
        return new ItemPedidoPeca(resultSet.getLong("peca_codigo"), resultSet.getInt("quantidade"));
    }
}
