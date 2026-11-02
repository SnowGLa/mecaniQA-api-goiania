package br.com.mecaniQA.api.jdbc.mapper;

import br.com.mecaniQA.api.model.Servico;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public final class ServicoResultSetMapper {
    private ServicoResultSetMapper() {}

    public static Servico map(ResultSet resultSet) throws SQLException {
        return Servico.reconstituir(
                resultSet.getLong("codigo"),
                resultSet.getString("nome"),
                resultSet.getInt("tempo_estimado_minutos"),
                JdbcValues.moeda(resultSet, "custo_tabelado"),
                JdbcValues.data(resultSet, "data_criacao"),
                JdbcValues.data(resultSet, "data_ultima_atualizacao"),
                resultSet.getLong("versao"));
    }

    public static void bind(PreparedStatement statement, Servico entidade) throws SQLException {
        statement.setString(1, entidade.getNome());
        statement.setInt(2, entidade.getTempoEstimadoMinutos());
        JdbcValues.moeda(statement, 3, entidade.getCustoTabelado());
        statement.setTimestamp(4, Timestamp.valueOf(entidade.getDataCriacao()));
        statement.setTimestamp(5, Timestamp.valueOf(entidade.getDataUltimaAtualizacao()));
    }
}
