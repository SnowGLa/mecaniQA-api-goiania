package br.com.mecaniQA.api.jdbc;

import br.com.mecaniQA.api.exception.PersistenciaException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class JdbcConnectionFactory {
    private final String url, usuario, senha;

    public JdbcConnectionFactory(
            @Value("${db.url}") String url,
            @Value("${db.username}") String usuario,
            @Value("${db.password}") String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    public Connection abrir() {
        try {
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException excecao) {
            throw new PersistenciaException(
                    "Nao foi possivel conectar ao banco de dados.", excecao);
        }
    }
}
