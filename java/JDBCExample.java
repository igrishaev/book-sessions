import java.sql.*;

public class JDBCExample {

    static final String DB_URL = "jdbc:postgresql://127.0.0.1/testdb";
    static final String USER = "ivan";
    static final String PASS = "****";
    static final String QUERY = "SELECT id, name, email, age FROM users";

    public static void main(String[] args) {

        try {

            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(QUERY);

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Age: " + rs.getInt("age"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
