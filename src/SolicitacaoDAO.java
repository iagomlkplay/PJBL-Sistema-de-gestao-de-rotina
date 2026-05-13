import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SolicitacaoDAO {
    public void inserir(SolicitacaoMudanca solicitacao) throws SQLException {
        String sql = "INSERT INTO solicitacoes (justificativa, status, data_criacao, dev_solicitante_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, solicitacao.getJustificativa());
            stmt.setString(2, solicitacao.getStatus().name());
            stmt.setTimestamp(3, new Timestamp(solicitacao.getDataCriacao().getTime()));
            stmt.setInt(4, solicitacao.getSolicitante().getId());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                solicitacao.setId(rs.getInt(1));
            }
        }
    }

    public List<SolicitacaoMudanca> listarTodos() throws SQLException {
        List<SolicitacaoMudanca> lista = new ArrayList<>();
        String sql = "SELECT * FROM solicitacoes";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            while (rs.next()) {
                int solicitanteId = rs.getInt("dev_solicitante_id");
                UsuarioDev solicitante = (UsuarioDev) usuarioDAO.buscarPorId(solicitanteId);
                SolicitacaoMudanca s = new SolicitacaoMudanca(rs.getString("justificativa"), solicitante);
                s.setId(rs.getInt("id"));
                s.setStatus(StatusSolicitacao.valueOf(rs.getString("status")));
                s.setDataCriacao(rs.getTimestamp("data_criacao"));
                lista.add(s);
            }
        }
        return lista;
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM solicitacoes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
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