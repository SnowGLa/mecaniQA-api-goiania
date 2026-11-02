package br.com.mecaniQA.api.jdbc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.mecaniQA.api.exception.ConflitoDadosException;
import br.com.mecaniQA.api.exception.EntidadeNaoEncontradaException;
import br.com.mecaniQA.api.exception.PersistenciaException;
import br.com.mecaniQA.api.model.CategoriaPeca;
import br.com.mecaniQA.api.model.EntidadePersistente;
import br.com.mecaniQA.api.model.ItemPedidoPeca;
import br.com.mecaniQA.api.model.OrdemServico;
import br.com.mecaniQA.api.model.Peca;
import br.com.mecaniQA.api.model.PedidoPecas;
import br.com.mecaniQA.api.model.Servico;
import br.com.mecaniQA.api.repository.OrdemServicoRepository;
import br.com.mecaniQA.api.repository.PecaRepository;
import br.com.mecaniQA.api.repository.PedidoPecasRepository;
import br.com.mecaniQA.api.repository.Repository;
import br.com.mecaniQA.api.repository.ServicoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Exercita o ciclo JDBC com doubles; nao substitui os testes MySQL reais. */
class JdbcRepositoryTests {
    private final JdbcConnectionFactory factory = mock(JdbcConnectionFactory.class);
    private final Connection connection = mock(Connection.class);
    private final List<PreparedStatement> statements = new ArrayList<>();
    private final List<ResultSet> results = new ArrayList<>();
    private boolean ausente;
    private int afetadas = 1;
    private String falharBatch;
    private final LocalDateTime data = LocalDateTime.of(2026, 1, 2, 3, 4, 5, 123456000);

    private void preparar() throws Exception {
        when(factory.abrir()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenAnswer(i -> statement(i.getArgument(0)));
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenAnswer(i -> statement(i.getArgument(0)));
    }

    private PreparedStatement statement(String sql) throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        statements.add(ps);
        when(ps.executeUpdate()).thenReturn(afetadas);
        when(ps.getGeneratedKeys()).thenAnswer(i -> linha("keys"));
        when(ps.executeQuery()).thenAnswer(i -> linha(sql));
        if (falharBatch != null && sql.startsWith(falharBatch)) {
            when(ps.executeBatch()).thenThrow(new SQLException("detalhe interno", "23000"));
        }
        return ps;
    }

    private ResultSet linha(String sql) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        results.add(rs);
        when(rs.next()).thenReturn(!ausente, false);
        when(rs.getLong(anyString())).thenAnswer(i -> i.getArgument(0).equals("versao") ? 0L : 42L);
        when(rs.getLong(anyInt())).thenReturn(42L);
        when(rs.getInt(anyString())).thenReturn(3);
        when(rs.getString(anyString()))
                .thenAnswer(
                        i ->
                                switch ((String) i.getArgument(0)) {
                                    case "categoria" -> "MOTOR";
                                    case "status" ->
                                            sql.contains("pedido_pecas") ? "ORCANDO" : "ABERTO";
                                    default -> "Exemplo";
                                });
        when(rs.getBigDecimal(anyString())).thenReturn(new BigDecimal("12.34"));
        when(rs.getTimestamp(anyString())).thenReturn(Timestamp.valueOf(data));
        return rs;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Repository repo(String tipo) {
        return switch (tipo) {
            case "peca" -> new PecaRepository(factory);
            case "servico" -> new ServicoRepository(factory);
            case "ordem" -> new OrdemServicoRepository(factory);
            default -> new PedidoPecasRepository(factory);
        };
    }

    private EntidadePersistente nova(String tipo) {
        return switch (tipo) {
            case "peca" -> new Peca("Filtro", "123", "Marca", CategoriaPeca.MOTOR, 10.0, 20.0);
            case "servico" -> new Servico("Revisao", 30, 100.0);
            case "ordem" ->
                    OrdemServico.builder()
                            .codigosServicos(List.of(42L))
                            .codigosPecas(List.of(42L))
                            .build();
            default -> new PedidoPecas(List.of(new ItemPedidoPeca(42L, 3)));
        };
    }

    private void fechados() throws Exception {
        verify(connection).close();
        for (var ps : statements) {
            verify(ps).close();
        }
        for (var rs : results) {
            verify(rs).close();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"peca", "servico", "ordem", "pedido"})
    @SuppressWarnings("unchecked")
    void cadastroUsaChaveGeradaReconstituiEFechaRecursos(String tipo) throws Exception {
        preparar();
        var novo = nova(tipo);
        var salvo = (EntidadePersistente) repo(tipo).save(novo);
        assertNull(novo.getCodigo());
        assertEquals(42L, salvo.getCodigo());
        assertEquals(data, salvo.getDataCriacao());
        assertEquals(data, salvo.getDataUltimaAtualizacao());
        verify(connection)
                .prepareStatement(startsWith("INSERT"), eq(Statement.RETURN_GENERATED_KEYS));
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection, never()).rollback();
        fechados();
    }

    @ParameterizedTest
    @ValueSource(strings = {"peca", "servico", "ordem", "pedido"})
    @SuppressWarnings("unchecked")
    void buscaListagemAtualizacaoEExclusaoUsamTemplate(String tipo) throws Exception {
        preparar();
        Repository repositorio = repo(tipo);
        var entidade = (EntidadePersistente) repositorio.findById(42L).orElseThrow();
        assertEquals(42L, entidade.getCodigo());
        List<?> lista = repositorio.findAll();
        assertEquals(1, lista.size());
        assertThrows(UnsupportedOperationException.class, lista::clear);
        assertNotNull(repositorio.update(entidade));
        assertTrue(repositorio.deleteById(42L));
        verify(connection, times(4)).commit();
        verify(connection, times(4)).close();
        for (var ps : statements) {
            verify(ps).close();
        }
        for (var rs : results) {
            verify(rs).close();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ordem", "pedido"})
    @SuppressWarnings("unchecked")
    void falhaEmAssociacaoFazRollbackDeTodoCadastro(String tipo) throws Exception {
        falharBatch =
                tipo.equals("ordem") ? "INSERT INTO ordem_servico_peca" : "INSERT INTO pedido_peca";
        preparar();
        assertThrows(ConflitoDadosException.class, () -> repo(tipo).save(nova(tipo)));
        verify(connection).rollback();
        verify(connection, never()).commit();
        fechados();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ordem", "pedido"})
    @SuppressWarnings("unchecked")
    void falhaEmAssociacaoFazRollbackDaAtualizacao(String tipo) throws Exception {
        preparar();
        var entidade = (EntidadePersistente) repo(tipo).findById(42L).orElseThrow();
        clearInvocations(connection);
        statements.clear();
        results.clear();
        falharBatch =
                tipo.equals("ordem") ? "INSERT INTO ordem_servico_peca" : "INSERT INTO pedido_peca";
        assertThrows(ConflitoDadosException.class, () -> repo(tipo).update(entidade));
        verify(connection).rollback();
        verify(connection, never()).commit();
        fechados();
    }

    @Test
    void falhaAoPrepararFechaConexaoEFazRollback() throws Exception {
        preparar();
        when(connection.prepareStatement(anyString()))
                .thenThrow(new SQLException("interno", "08006"));
        assertThrows(PersistenciaException.class, () -> new ServicoRepository(factory).findAll());
        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void falhaNoMapeamentoFechaResultSetEStatement() throws Exception {
        preparar();
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("codigo")).thenThrow(new SQLException("interno"));
        assertThrows(PersistenciaException.class, () -> new ServicoRepository(factory).findAll());
        verify(rs).close();
        verify(ps).close();
        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void falhaDeCommitTentaRollbackEFechaRecursos() throws Exception {
        preparar();
        doThrow(new SQLException("commit")).when(connection).commit();
        assertThrows(
                PersistenciaException.class, () -> new ServicoRepository(factory).deleteById(42L));
        verify(connection).rollback();
        fechados();
    }

    @Test
    void ausenciaDeChaveGeradaNaoPermiteCommit() throws Exception {
        preparar();
        ausente = true;
        assertThrows(
                PersistenciaException.class,
                () -> new ServicoRepository(factory).save(new Servico()));
        verify(connection).rollback();
        verify(connection, never()).commit();
        fechados();
    }

    @Test
    void atualizacaoDeVersaoDesatualizadaGeraConflito() throws Exception {
        preparar();
        afetadas = 0;
        var servico = Servico.reconstituir(42L, "S", 1, 1.0, data, data, 0);
        assertThrows(
                ConflitoDadosException.class, () -> new ServicoRepository(factory).update(servico));
        verify(connection).rollback();
        fechados();
    }

    @Test
    void atualizacaoInexistenteGeraExcecao404() throws Exception {
        preparar();
        afetadas = 0;
        ausente = true;
        assertThrows(
                EntidadeNaoEncontradaException.class,
                () ->
                        new ServicoRepository(factory)
                                .update(Servico.reconstituir(42L, "S", 1, 1.0, data, data, 0)));
        verify(connection).rollback();
        fechados();
    }

    @Test
    void exclusaoInexistenteRetornaFalse() throws Exception {
        preparar();
        afetadas = 0;
        assertFalse(new PecaRepository(factory).deleteById(42L));
        fechados();
    }
}
