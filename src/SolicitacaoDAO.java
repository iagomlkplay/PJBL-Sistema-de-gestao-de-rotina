import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SolicitacaoDAO {
    public void inserir(Solicitacao solicitacao) throws SQLException {
        String sql = "INSERT INTO solicitacoes (justificativa, status, data_criacao, dev_solicitante_id, tarefa_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, solicitacao.getJustificativa());
            stmt.setString(2, solicitacao.getStatus().name());
            stmt.setTimestamp(3, new Timestamp(solicitacao.getDataCriacao().getTime()));
            stmt.setInt(4, solicitacao.getSolicitante().getId());
            if (solicitacao.getTarefaRelacionada() != null) {
                stmt.setInt(5, solicitacao.getTarefaRelacionada().getId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                solicitacao.setId(rs.getInt(1));
            }
        }
    }

    public List<Solicitacao> listarTodos() throws SQLException {
        List<Solicitacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM solicitacoes";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            TarefaDAO tarefaDAO = new TarefaDAO();
            ProjetoDAO projetoDAO = new ProjetoDAO();
            while (rs.next()) {
                int solicitanteId = rs.getInt("dev_solicitante_id");
                UsuarioDev solicitante = (UsuarioDev) usuarioDAO.buscarPorId(solicitanteId);
                int tarefaId = rs.getInt("tarefa_id");
                Tarefa tarefa = tarefaId != 0 ? tarefaDAO.buscarPorId(tarefaId, usuarioDAO, projetoDAO) : null;
                Solicitacao s = new Solicitacao(rs.getString("justificativa"), solicitante, tarefa);
                s.setId(rs.getInt("id"));
                s.setStatus(StatusSolicitacao.valueOf(rs.getString("status")));
                s.setDataCriacao(rs.getTimestamp("data_criacao"));
                lista.add(s);
            }
        }
        return lista;
    }

    public List<Solicitacao> listarPorGestor(int gestorId) throws SQLException {
        List<Solicitacao> lista = new ArrayList<>();
        String sql = "SELECT s.* FROM solicitacoes s " +
                "JOIN usuarios u ON s.dev_solicitante_id = u.id " +
                "WHERE u.gestor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gestorId);
            ResultSet rs = stmt.executeQuery();
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            TarefaDAO tarefaDAO = new TarefaDAO();
            ProjetoDAO projetoDAO = new ProjetoDAO();
            while (rs.next()) {
                int solicitanteId = rs.getInt("dev_solicitante_id");
                UsuarioDev solicitante = (UsuarioDev) usuarioDAO.buscarPorId(solicitanteId);
                int tarefaId = rs.getInt("tarefa_id");
                Tarefa tarefa = tarefaId != 0 ? tarefaDAO.buscarPorId(tarefaId, usuarioDAO, projetoDAO) : null;
                Solicitacao s = new Solicitacao(rs.getString("justificativa"), solicitante, tarefa);
                s.setId(rs.getInt("id"));
                s.setStatus(StatusSolicitacao.valueOf(rs.getString("status")));
                s.setDataCriacao(rs.getTimestamp("data_criacao"));
                lista.add(s);
            }
        }
        return lista;
    }

    public void atualizarStatus(int id, StatusSolicitacao novoStatus) throws SQLException {
        String sql = "UPDATE solicitacoes SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }
}