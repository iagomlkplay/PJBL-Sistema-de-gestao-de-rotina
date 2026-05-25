import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public void inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, cpf, email, senha, tipo, departamento, gestor_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getSenha());
            stmt.setString(5, usuario.getTipoUsuario().name());
            if (usuario instanceof UsuarioGestor) {
                stmt.setString(6, ((UsuarioGestor) usuario).getDepartamento());
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setNull(6, Types.VARCHAR);
                stmt.setInt(7, ((UsuarioDev) usuario).getGestorId());
            }
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                usuario.setId(rs.getInt(1));
            }
        }
    }

    public Usuario autenticar(String email, String senha) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND senha = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return construirUsuario(rs);
            }
        }
        return null;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return construirUsuario(rs);
            }
        }
        return null;
    }

    public List<UsuarioDev> listarDevsPorGestor(int gestorId) throws SQLException {
        List<UsuarioDev> devs = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE tipo = 'DEV' AND gestor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gestorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                devs.add((UsuarioDev) construirUsuario(rs));
            }
        }
        return devs;
    }

    // Método para uma futura implementação. Por hora, o sistema não permite trocar o gestor de um desenvolvedor após o cadastro.
    public void atualizarGestorDoDev(int devId, int novoGestorId) throws SQLException {
        String sql = "UPDATE usuarios SET gestor_id = ? WHERE id = ? AND tipo = 'DEV'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novoGestorId);
            stmt.setInt(2, devId);
            stmt.executeUpdate();
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                usuarios.add(construirUsuario(rs));
            }
        }
        return usuarios;
    }

    private Usuario construirUsuario(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String cpf = rs.getString("cpf");
        String email = rs.getString("email");
        String senha = rs.getString("senha");
        String tipo = rs.getString("tipo");
        if ("GESTOR".equals(tipo)) {
            String departamento = rs.getString("departamento");
            UsuarioGestor gestor = new UsuarioGestor(id, nome, cpf, email, senha, departamento);
            gestor.setTipoUsuario(TipoUsuario.GESTOR);
            return gestor;
        } else {
            int gestorId = rs.getInt("gestor_id");
            UsuarioDev dev = new UsuarioDev(id, nome, cpf, email, senha);
            dev.setGestorId(gestorId);
            dev.setTipoUsuario(TipoUsuario.DEV);
            return dev;
        }
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return construirUsuario(rs);
            }
        }
        return null;
    }

    public Usuario buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE cpf = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return construirUsuario(rs);
            }
        }
        return null;
    }

    public void atualizarUsuario(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nome = ?, cpf = ?, email = ?, senha = ?, tipo = ?, departamento = ?, gestor_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getSenha());
            stmt.setString(5, usuario.getTipoUsuario().name());
            if (usuario instanceof UsuarioGestor) {
                stmt.setString(6, ((UsuarioGestor) usuario).getDepartamento());
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setNull(6, Types.VARCHAR);
                stmt.setInt(7, ((UsuarioDev) usuario).getGestorId());
            }
            stmt.setInt(8, usuario.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}