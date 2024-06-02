import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;

public class KeuanganApp extends JFrame {
    private JLabel saldoLabel;
    private JTextField inputField;
    private JButton tambahButton;
    private JButton kurangiButton;
    private JTable pemasukanTable;
    private JTable pengeluaranTable;
    private DefaultTableModel pemasukanModel;
    private DefaultTableModel pengeluaranModel;
    private double saldo = 0.0;
    private Connection conn;

    public KeuanganApp() {
        if (!showLoginDialog()) {
            System.exit(0);
        }

        // Koneksi ke database
        String DB_URL = "jdbc:mysql://localhost:3306/keuangan_db";
        String DB_USER = "root";
        String DB_PASSWORD = "password";

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createTable(); // Buat tabel jika belum ada
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // GUI
        setTitle("Aplikasi Pencatatan Keuangan");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Posisi di tengah layar
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setBackground(new Color(255, 240, 245)); // Soft pink
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        saldoLabel = new JLabel("Saldo: " + saldo);
        inputPanel.add(saldoLabel);

        inputField = new JTextField(10);
        inputPanel.add(inputField);

        tambahButton = new JButton("Tambah Pemasukan");
        tambahButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double jumlah = Double.parseDouble(inputField.getText());
                tambahPemasukan(jumlah);
            }
        });
        inputPanel.add(tambahButton);

        kurangiButton = new JButton("Kurangi Pengeluaran");
        kurangiButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double jumlah = Double.parseDouble(inputField.getText());
                kurangiPengeluaran(jumlah);
            }
        });
        inputPanel.add(kurangiButton);

        add(inputPanel, BorderLayout.NORTH);

        // Panel untuk tabel pemasukan
        JPanel pemasukanPanel = new JPanel(new BorderLayout());
        pemasukanPanel.setBorder(BorderFactory.createTitledBorder("Pemasukan"));
        pemasukanPanel.setBackground(new Color(173, 216, 230)); // Baby blue

        pemasukanModel = new DefaultTableModel(new Object[]{"ID", "Jumlah", "Tanggal"}, 0);
        pemasukanTable = new JTable(pemasukanModel);
        JScrollPane pemasukanScrollPane = new JScrollPane(pemasukanTable);
        pemasukanPanel.add(pemasukanScrollPane);

        add(pemasukanPanel, BorderLayout.CENTER);

        // Panel untuk tabel pengeluaran
        JPanel pengeluaranPanel = new JPanel(new BorderLayout());
        pengeluaranPanel.setBorder(BorderFactory.createTitledBorder("Pengeluaran"));
        pengeluaranPanel.setBackground(new Color(255, 255, 153)); // Kuning pastel

        pengeluaranModel = new DefaultTableModel(new Object[]{"ID", "Jumlah", "Tanggal"}, 0);
        pengeluaranTable = new JTable(pengeluaranModel);
        JScrollPane pengeluaranScrollPane = new JScrollPane(pengeluaranTable);
        pengeluaranPanel.add(pengeluaranScrollPane);

        add(pengeluaranPanel, BorderLayout.SOUTH);

        setVisible(true);
        tampilkanTransaksi();
    }

    private void createTable() {
        try {
            Statement stmt = conn.createStatement();
            String createTableSQL = "CREATE TABLE IF NOT EXISTS transaksi (" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "Tipe VARCHAR(20), " +
                    "Jumlah DECIMAL(10, 2), " +
                    "Tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            stmt.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void tambahPemasukan(double jumlah) {
        saldo += jumlah;
        saldoLabel.setText("Saldo: " + saldo);
        Date tanggal = new Date();
        pemasukanModel.addRow(new Object[]{pemasukanModel.getRowCount() + 1, jumlah, tanggal});

        // Simpan transaksi ke database
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO transaksi (Tipe, Jumlah, Tanggal) VALUES (?, ?, ?)");
            stmt.setString(1, "Pemasukan");
            stmt.setDouble(2, jumlah);
            stmt.setTimestamp(3, new java.sql.Timestamp(tanggal.getTime()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void kurangiPengeluaran(double jumlah) {
        saldo -= jumlah;
        saldoLabel.setText("Saldo: " + saldo);
        Date tanggal = new Date();
        pengeluaranModel.addRow(new Object[]{pengeluaranModel.getRowCount() + 1, jumlah, tanggal});

        // Simpan transaksi ke database
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO transaksi (Tipe, Jumlah, Tanggal) VALUES (?, ?, ?)");
            stmt.setString(1, "Pengeluaran");
            stmt.setDouble(2, jumlah);
            stmt.setTimestamp(3, new java.sql.Timestamp(tanggal.getTime()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void tampilkanTransaksi() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM transaksi");

            while (rs.next()) {
                int id = rs.getInt("ID");
                String tipe = rs.getString("Tipe");
                double jumlah = rs.getDouble("Jumlah");
                Date tanggal = rs.getTimestamp("Tanggal");

                if (tipe.equals("Pemasukan")) {
                    pemasukanModel.addRow(new Object[]{id, jumlah, tanggal});
                } else if (tipe.equals("Pengeluaran")) {
                    pengeluaranModel.addRow(new Object[]{id, jumlah, tanggal});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean showLoginDialog() {
        JTextField usernameField = new JTextField(10);
        JPasswordField passwordField = new JPasswordField(10);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2));
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, loginPanel, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if ((username.equals("Zuher") && password.equals("12345")) || (username.equals("Zetcha") && password.equals("67890"))) {
                JOptionPane.showMessageDialog(this, "Anda berhasil login!");
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Username atau password anda salah", "Login Gagal", JOptionPane.ERROR_MESSAGE);
                return showLoginDialog(); // Tampilkan dialog lagi jika login gagal
            }
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new KeuanganApp();
            }
        });
    }
}
