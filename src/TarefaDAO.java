import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TarefaDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public void inserir(Tarefa tarefa) throws SQLException {
        String sql = "INSERT INTO tarefas (descricao, prazo, nivel_importancia, status, horas_estimadas, horas_trabalhadas, dev_responsavel_id, projeto_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tarefa.getDescricao());
            stmt.setDate(2, new java.sql.Date(tarefa.getPrazo().getTime()));
            stmt.setString(3, tarefa.getNivelImportancia().name());
            stmt.setString(4, tarefa.getStatus().name());
            stmt.setDouble(5, tarefa.getHorasEstimadas());
            stmt.setDouble(6, tarefa.getHorasTrabalhadas());
            stmt.setInt(7, tarefa.getDevResponsavel().getId());
            if (tarefa.getProjetoPai() != null) {
                stmt.setInt(8, tarefa.getProjetoPai().getId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                tarefa.setId(rs.getInt(1));
            }
        }
    }

    public Tarefa buscarPorId(int id, UsuarioDAO usuarioDAO, ProjetoDAO projetoDAO) throws SQLException {
        String sql = "SELECT * FROM tarefas WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UsuarioDev dev = (UsuarioDev) usuarioDAO.buscarPorId(rs.getInt("dev_responsavel_id"));
                int projId = rs.getInt("projeto_id");
                Projeto projeto = (projId != 0) ? projetoDAO.buscarPorId(projId) : null;
                return new Tarefa(
                        rs.getInt("id"),
                        rs.getString("descricao"),
                        rs.getDate("prazo"),
                        NivelImportancia.valueOf(rs.getString("nivel_importancia")),
                        StatusTarefa.valueOf(rs.getString("status")),
                        dev,
                        rs.getDouble("horas_estimadas"),
                        rs.getDouble("horas_trabalhadas"),
                        projeto
                );
            }
        }
        return null;
    }

    public List<Tarefa> listarPorDev(int devId, UsuarioDAO usuarioDAO, ProjetoDAO projetoDAO) throws SQLException {
        List<Tarefa> lista = new ArrayList<>();
        String sql = "SELECT * FROM tarefas WHERE dev_responsavel_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, devId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int projId = rs.getInt("projeto_id");
                Projeto projeto = (projId != 0) ? projetoDAO.buscarPorId(projId) : null;
                UsuarioDev dev = (UsuarioDev) usuarioDAO.buscarPorId(devId);
                lista.add(new Tarefa(
                        rs.getInt("id"),
                        rs.getString("descricao"),
                        rs.getDate("prazo"),
                        NivelImportancia.valueOf(rs.getString("nivel_importancia")),
                        StatusTarefa.valueOf(rs.getString("status")),
                        dev,
                        rs.getDouble("horas_estimadas"),
                        rs.getDouble("horas_trabalhadas"),
                        projeto
                ));
            }
        }
        return lista;
    }

    // Listar tarefas por projeto
    public List<Tarefa> listarPorProjeto(int projetoId, UsuarioDAO usuarioDAO, ProjetoDAO projetoDAO) throws SQLException {
        List<Tarefa> lista = new ArrayList<>();
        String sql = "SELECT * FROM tarefas WHERE projeto_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projetoId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UsuarioDev dev = (UsuarioDev) usuarioDAO.buscarPorId(rs.getInt("dev_responsavel_id"));
                Projeto projeto = projetoDAO.buscarPorId(projetoId);
                lista.add(new Tarefa(
                        rs.getInt("id"),
                        rs.getString("descricao"),
                        rs.getDate("prazo"),
                        NivelImportancia.valueOf(rs.getString("nivel_importancia")),
                        StatusTarefa.valueOf(rs.getString("status")),
                        dev,
                        rs.getDouble("horas_estimadas"),
                        rs.getDouble("horas_trabalhadas"),
                        projeto
                ));
            }
        }
        return lista;
    }

    public List<Tarefa> listarTodas() throws SQLException {
        List<Tarefa> lista = new ArrayList<>();
        String sql = "SELECT * FROM tarefas";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            ProjetoDAO projetoDAO = new ProjetoDAO();
            while (rs.next()) {
                int devId = rs.getInt("dev_responsavel_id");
                UsuarioDev dev = (UsuarioDev) usuarioDAO.buscarPorId(devId);
                int projId = rs.getInt("projeto_id");
                Projeto projeto = (projId != 0) ? projetoDAO.buscarPorId(projId) : null;

                Tarefa t = new Tarefa(
                        rs.getInt("id"),
                        rs.getString("descricao"),
                        rs.getDate("prazo"),
                        NivelImportancia.valueOf(rs.getString("nivel_importancia")),
                        StatusTarefa.valueOf(rs.getString("status")),
                        dev,
                        rs.getDouble("horas_estimadas"),
                        rs.getDouble("horas_trabalhadas"),
                        projeto
                );
                lista.add(t);
            }
        }
        return lista;
    }

    public void atualizarStatus(int id, StatusTarefa novoStatus) throws SQLException {
        String sql = "UPDATE tarefas SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void adicionarHorasTrabalhadas(int id, double horas) throws SQLException {
        String sql = "UPDATE tarefas SET horas_trabalhadas = horas_trabalhadas + ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, horas);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void reatribuirDev(int tarefaId, int novoDevId) throws SQLException {
        String sql = "UPDATE tarefas SET dev_responsavel_id = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novoDevId);
            stmt.setInt(2, tarefaId);
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM tarefas WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}