import java.sql.*;

public class UpdateExample {

    static final String DB_URL = "jdbc:postgresql://127.0.0.1/test";
    static final String USER = "book";
    static final String PASS = "book";
    static final String QUERY = "SELECT * FROM users";

    public static void main(String[] args) {

        try {

            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);

            Statement stmt = conn.createStatement();
            String sql = "insert into users (id) values (default)";

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
