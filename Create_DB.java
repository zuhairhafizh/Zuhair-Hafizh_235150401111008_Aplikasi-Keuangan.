import java.sql.*;

public class Create_DB {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "29September05.";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Create database
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS keuangan_db");

            // Use the created database
            statement.executeUpdate("USE keuangan_db");

            // Create the transaksi table
            String createTransaksiTable = "CREATE TABLE IF NOT EXISTS transaksi (" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "Tipe VARCHAR(20), " +
                    "Jumlah DECIMAL(10, 2), " +
                    "Waktu TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            statement.executeUpdate(createTransaksiTable);

            System.out.println("Database setup completed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
