package br.com.mecaniQA.api.repository;

import br.com.mecaniQA.api.jdbc.AbstractJdbcRepository;
import br.com.mecaniQA.api.jdbc.JdbcConnectionFactory;
import br.com.mecaniQA.api.jdbc.mapper.PecaResultSetMapper;
import br.com.mecaniQA.api.model.Peca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@org.springframework.stereotype.Repository
public class PecaRepository extends AbstractJdbcRepository<Peca> {
    public PecaRepository(JdbcConnectionFactory conexoes) {
        super(conexoes);
    }

    @Override
    protected String tabela() {
        return "peca";
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO peca"
                + " (nome,codigo_barras,fornecedor_marca,categoria,quantidade_estoque,preco_custo,"
                + "preco_venda,tamanho,cor,data_cadastro,data_ultima_atualizacao)"
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE peca SET"
                + " nome=?,codigo_barras=?,fornecedor_marca=?,categoria=?,quantidade_estoque=?,"
                + "preco_custo=?,preco_venda=?,tamanho=?,cor=?,data_cadastro=?,"
                + "data_ultima_atualizacao=?,versao=versao+1"
                + " WHERE codigo=? AND versao=?";
    }

    @Override
    protected void bindInsert(PreparedStatement statement, Peca entidade) throws SQLException {
        PecaResultSetMapper.bind(statement, entidade);
    }

    @Override
    protected void bindUpdate(PreparedStatement statement, Peca entidade) throws SQLException {
        PecaResultSetMapper.bind(statement, entidade);
        statement.setLong(12, entidade.getCodigo());
        statement.setLong(13, entidade.getVersao());
    }

    @Override
    protected Peca map(ResultSet resultSet, Connection conexao) throws SQLException {
        return PecaResultSetMapper.map(resultSet);
    }
}
