import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gestao_rotina?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root"; // se você tiver senha, coloque aqui

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);

        // Tentar usar o try catch

        //try {
        //    return DriverManager.getConnection(URL, USER, PASSWORD);
        //} catch (SQLException e) {
        //    throw new RuntimeException(e);

    }
}