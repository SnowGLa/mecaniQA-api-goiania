package br.com.mecaniQA.api.jdbc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.mecaniQA.api.exception.PersistenciaException;
import br.com.mecaniQA.api.jdbc.mapper.ItemPedidoPecaResultSetMapper;
import br.com.mecaniQA.api.jdbc.mapper.OrdemServicoResultSetMapper;
import br.com.mecaniQA.api.jdbc.mapper.PecaResultSetMapper;
import br.com.mecaniQA.api.jdbc.mapper.PedidoPecasResultSetMapper;
import br.com.mecaniQA.api.model.CategoriaPeca;
import br.com.mecaniQA.api.model.ItemPedidoPeca;
import br.com.mecaniQA.api.model.StatusOrdemServico;
import br.com.mecaniQA.api.model.StatusPedidoPecas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

class ResultSetMapperTests {
    private ResultSet linha(String status) throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getLong("codigo")).thenReturn(91L);
        when(rs.getLong("versao")).thenReturn(7L);
        when(rs.getString("status")).thenReturn(status);
        when(rs.getTimestamp(anyString()))
                .thenReturn(Timestamp.valueOf(LocalDateTime.of(2020, 2, 3, 4, 5, 6, 123456000)));
        return rs;
    }

    @ParameterizedTest
    @EnumSource(StatusOrdemServico.class)
    void reconstituiCadaStatusDeOrdemSemGerarIdentidadeOuDatas(StatusOrdemServico status)
            throws Exception {
        var rs = linha(status.name());
        var ordem = OrdemServicoResultSetMapper.map(rs, List.of(3L), List.of(4L));
        assertEquals(91L, ordem.getCodigo());
        assertEquals(7, ordem.getVersao());
        assertEquals(status, ordem.getStatus());
        assertEquals(rs.getTimestamp("data_criacao").toLocalDateTime(), ordem.getDataCriacao());
        assertEquals(List.of(3L), ordem.getCodigosServicos());
        assertEquals(List.of(4L), ordem.getCodigosPecas());
    }

    @ParameterizedTest
    @EnumSource(StatusPedidoPecas.class)
    void reconstituiCadaStatusEItensDoPedido(StatusPedidoPecas status) throws Exception {
        var pedido =
                PedidoPecasResultSetMapper.map(
                        linha(status.name()), List.of(new ItemPedidoPeca(5L, 3)));
        assertEquals(status, pedido.getStatus());
        assertEquals(91L, pedido.getCodigo());
        assertEquals(3, pedido.getItens().getFirst().getQuantidade());
    }

    @ParameterizedTest
    @EnumSource(CategoriaPeca.class)
    void converteCategoriasComSeguranca(CategoriaPeca categoria) throws Exception {
        var rs = linha(null);
        when(rs.getString("categoria")).thenReturn(categoria.name());
        assertEquals(categoria, PecaResultSetMapper.map(rs).getCategoria());
    }

    @Test
    void valorEnumeradoInvalidoViraFalhaDePersistencia() throws Exception {
        var rs = linha("DESCONHECIDO");
        assertThrows(
                PersistenciaException.class,
                () -> OrdemServicoResultSetMapper.map(rs, List.of(), List.of()));
        when(rs.getString("status")).thenReturn(null);
        assertThrows(
                PersistenciaException.class, () -> PedidoPecasResultSetMapper.map(rs, List.of()));
    }

    @Test
    void dataObrigatoriaNulaViraFalhaDePersistencia() throws Exception {
        var rs = linha("ABERTO");
        when(rs.getTimestamp(anyString())).thenReturn(null);
        assertThrows(
                PersistenciaException.class,
                () -> OrdemServicoResultSetMapper.map(rs, List.of(), List.of()));
    }

    @Test
    void mapperDeItemPreservaCodigoDaPecaEQuantidade() throws Exception {
        var rs = mock(ResultSet.class);
        when(rs.getLong("peca_codigo")).thenReturn(15L);
        when(rs.getInt("quantidade")).thenReturn(8);
        var item = ItemPedidoPecaResultSetMapper.map(rs);
        assertEquals(15L, item.getCodigoPeca());
        assertEquals(8, item.getQuantidade());
    }

    @Test
    void driverManagerAbreConexoesDistintasEProtegeErro() throws Exception {
        Driver driver = mock(Driver.class);
        String url = "jdbc:oat3-test:conexao";
        when(driver.connect(eq(url), any()))
                .thenReturn(mock(Connection.class), mock(Connection.class));
        DriverManager.registerDriver(driver);
        try {
            var factory = new JdbcConnectionFactory(url, "teste", "nao-e-uma-senha-real");
            try (var primeira = factory.abrir();
                    var segunda = factory.abrir()) {
                assertNotSame(primeira, segunda);
            }
            when(driver.connect(eq(url), any()))
                    .thenThrow(new SQLException("dados internos", "28000"));
            var erro = assertThrows(PersistenciaException.class, factory::abrir);
            assertFalse(erro.getMessage().contains(url));
            assertNotNull(erro.getCause());
        } finally {
            DriverManager.deregisterDriver(driver);
        }
    }
}
