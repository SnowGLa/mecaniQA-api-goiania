package br.com.mecaniQA.api.repository;

import br.com.mecaniQA.api.jdbc.AbstractJdbcRepository;
import br.com.mecaniQA.api.jdbc.JdbcConnectionFactory;
import br.com.mecaniQA.api.jdbc.mapper.ServicoResultSetMapper;
import br.com.mecaniQA.api.model.Servico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@org.springframework.stereotype.Repository
public class ServicoRepository extends AbstractJdbcRepository<Servico> {
    public ServicoRepository(JdbcConnectionFactory conexoes) {
        super(conexoes);
    }

    @Override
    protected String tabela() {
        return "servico";
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO servico"
                   + " (nome,tempo_estimado_minutos,custo_tabelado,data_criacao,data_ultima_atualizacao)"
                   + " VALUES (?,?,?,?,?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE servico SET"
                + " nome=?,tempo_estimado_minutos=?,custo_tabelado=?,data_criacao=?,"
                + "data_ultima_atualizacao=?,versao=versao+1"
                + " WHERE codigo=? AND versao=?";
    }

    @Override
    protected void bindInsert(PreparedStatement statement, Servico entidade) throws SQLException {
        ServicoResultSetMapper.bind(statement, entidade);
    }

    @Override
    protected void bindUpdate(PreparedStatement statement, Servico entidade) throws SQLException {
        ServicoResultSetMapper.bind(statement, entidade);
        statement.setLong(6, entidade.getCodigo());
        statement.setLong(7, entidade.getVersao());
    }

    @Override
    protected Servico map(ResultSet resultSet, Connection conexao) throws SQLException {
        return ServicoResultSetMapper.map(resultSet);
    }
}
