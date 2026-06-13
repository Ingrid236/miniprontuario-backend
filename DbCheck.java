import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbCheck {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/miniprontuario_db";
        String user = "postgres";
        String password = "12345";
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name, email, cpf, cro FROM dentist")) {
                System.out.println("--- DENTISTAS CADASTRADOS ---");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("ID: " + rs.getString("id"));
                    System.out.println("Nome: " + rs.getString("name"));
                    System.out.println("Email: " + rs.getString("email"));
                    System.out.println("CPF: " + rs.getString("cpf"));
                    System.out.println("CRO: " + rs.getString("cro"));
                    System.out.println("----------------------------");
                }
                System.out.println("Total: " + count + " dentista(s) cadastrado(s).");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
