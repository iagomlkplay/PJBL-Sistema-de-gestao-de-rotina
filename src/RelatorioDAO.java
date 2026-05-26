import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RelatorioDAO {

    public void inserir(Relatorio relatorio) throws SQLException {
        String sql = "INSERT INTO relatorios (data_envio, conteudo, tarefa_id, projeto_id, dev_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, new Timestamp(relatorio.getDataEnvio().getTime()));
            stmt.setString(2, relatorio.getConteudo());
            if (relatorio.getTarefaRelacionada() != null) {
                stmt.setInt(3, relatorio.getTarefaRelacionada().getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            if (relatorio.getProjetoRelacionado() != null) {
                stmt.setInt(4, relatorio.getProjetoRelacionado().getId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setInt(5, relatorio.getDevRemetente().getId());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                relatorio.setId(rs.getInt(1));
            }
        }
    }

    public List<Relatorio> listarTodos() throws SQLException {
        List<Relatorio> lista = new ArrayList<>();
        String sql = "SELECT * FROM relatorios";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            TarefaDAO tarefaDAO = new TarefaDAO();
            ProjetoDAO projetoDAO = new ProjetoDAO();
            while (rs.next()) {
                int devId = rs.getInt("dev_id");
                UsuarioDev dev = (UsuarioDev) usuarioDAO.buscarPorId(devId);
                Relatorio r = new Relatorio(rs.getString("conteudo"), dev);
                r.setId(rs.getInt("id"));
                r.setDataEnvio(rs.getTimestamp("data_envio"));
                int tarefaId = rs.getInt("tarefa_id");
                if (tarefaId != 0) {
                    r.setTarefaRelacionada(tarefaDAO.buscarPorId(tarefaId, usuarioDAO, projetoDAO));
                }
                int projetoId = rs.getInt("projeto_id");
                if (projetoId != 0) {
                    r.setProjetoRelacionado(projetoDAO.buscarPorId(projetoId));
                }
                lista.add(r);
            }
        }
        return lista;
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM relatorios WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}