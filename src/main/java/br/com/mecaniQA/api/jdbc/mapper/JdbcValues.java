package br.com.mecaniQA.api.jdbc.mapper;

import br.com.mecaniQA.api.exception.PersistenciaException;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

public final class JdbcValues {
    private JdbcValues() {}

    public static LocalDateTime data(ResultSet resultSet, String coluna) throws SQLException {
        Timestamp valor = resultSet.getTimestamp(coluna);
        if (valor == null) {
            throw new PersistenciaException("Data obrigatoria ausente nos dados persistidos.");
        }
        return valor.toLocalDateTime();
    }

    public static <E extends Enum<E>> E enumeracao(
            ResultSet resultSet, String coluna, Class<E> tipo) throws SQLException {
        try {
            return Enum.valueOf(tipo, resultSet.getString(coluna));
        } catch (IllegalArgumentException | NullPointerException excecao) {
            throw new PersistenciaException("Valor enumerado persistido invalido.", excecao);
        }
    }

    public static Double moeda(ResultSet resultSet, String coluna) throws SQLException {
        BigDecimal valor = resultSet.getBigDecimal(coluna);
        return valor == null ? null : valor.doubleValue();
    }

    public static void moeda(PreparedStatement statement, int indice, Double valor)
            throws SQLException {
        if (valor == null) {
            statement.setNull(indice, Types.DECIMAL);
        } else {
            statement.setBigDecimal(indice, BigDecimal.valueOf(valor));
        }
    }
}
