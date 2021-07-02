import java.sql.*;

public class JDBCExample {

    static final String DB_URL = "jdbc:postgresql://127.0.0.1/test";
    static final String USER = "book";
    static final String PASS = "book";
    static final String QUERY = "SELECT * FROM users";

    public static void main(String[] args) {

        try {

            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(QUERY);

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("First name: " + rs.getString("fname"));
                System.out.println("Last name: " + rs.getString("lname"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Age: " + rs.getInt("age"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


// javac JDBCExample.java
// java -cp ../jars/postgresql-42.2.22.jar:. JDBCExample
// ID: 1
// First name: John
// Last name: Smith
// Email: test@test.com
// Age: 25
