import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjetoDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public void inserir(Projeto projeto) throws SQLException {
        String sql = "INSERT INTO projetos (nome, prazo, importancia, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, projeto.getNome());
            stmt.setDate(2, new java.sql.Date(projeto.getPrazo().getTime()));
            stmt.setString(3, projeto.getImportancia().name());
            stmt.setString(4, projeto.getStatus().name());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                projeto.setId(rs.getInt(1));
            }
        }
    }

    public Projeto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM projetos WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Projeto(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDate("prazo"),
                        NivelImportancia.valueOf(rs.getString("importancia")),
                        StatusTarefa.valueOf(rs.getString("status"))
                );
            }
        }
        return null;
    }

    public void atualizarStatus(int id, StatusTarefa novoStatus) throws SQLException {
        String sql = "UPDATE projetos SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public List<Projeto> listarTodos() throws SQLException {
        List<Projeto> lista = new ArrayList<>();
        String sql = "SELECT * FROM projetos";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Projeto(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDate("prazo"),
                        NivelImportancia.valueOf(rs.getString("importancia")),
                        StatusTarefa.valueOf(rs.getString("status"))
                ));
            }
        }
        return lista;
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM projetos WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}