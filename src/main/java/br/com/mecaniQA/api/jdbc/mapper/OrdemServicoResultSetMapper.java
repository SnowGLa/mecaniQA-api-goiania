package br.com.mecaniQA.api.jdbc.mapper;

import br.com.mecaniQA.api.model.OrdemServico;
import br.com.mecaniQA.api.model.StatusOrdemServico;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class OrdemServicoResultSetMapper {
    private OrdemServicoResultSetMapper() {}

    public static OrdemServico map(ResultSet resultSet, List<Long> servicos, List<Long> pecas)
            throws SQLException {
        return OrdemServico.reconstituir(
                resultSet.getLong("codigo"),
                JdbcValues.enumeracao(resultSet, "status", StatusOrdemServico.class),
                servicos,
                pecas,
                JdbcValues.data(resultSet, "data_criacao"),
                JdbcValues.data(resultSet, "data_ultima_atualizacao"),
                resultSet.getLong("versao"));
    }
}
