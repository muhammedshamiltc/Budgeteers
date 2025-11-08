import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AuthManager {
    // DBUtil class content
    public static class DBUtil {
        private static final String URL = "jdbc:mysql://localhost:3306/expensetracker?useSSL=false&allowPublicKeyRetrieval=true";
        private static final String USER = "root";
        private static final String PASSWORD = "toor123";

        public static Connection getConnection() throws Exception {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    // LoginFrame class content
    public static class LoginFrame extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public LoginFrame() {
            setTitle("Expense Manager - Login");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(480, 300);
            setLocationRelativeTo(null);

            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(new Color(42, 183, 172));
            JLabel title = new JLabel("Expenses");
            title.setFont(new Font("SansSerif", Font.BOLD, 30));
            title.setForeground(Color.WHITE);
            title.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
            top.add(title, BorderLayout.WEST);

            JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
            form.add(new JLabel("Username:"));
            usernameField = new JTextField();
            form.add(usernameField);
            form.add(new JLabel("Password:"));
            passwordField = new JPasswordField();
            form.add(passwordField);

            JButton loginBtn = new JButton("Login");
            JButton signUpBtn = new JButton("Sign Up");
            form.add(loginBtn);
            form.add(signUpBtn);

            add(top, BorderLayout.NORTH);
            add(form, BorderLayout.CENTER);

            loginBtn.addActionListener(e -> login());
            signUpBtn.addActionListener(e -> new SignupFrame());
        }

        private void login() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            try (Connection conn = AuthManager.DBUtil.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username=? AND password=?");
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    dispose();
                    new TrackerFrame(userId, username).setVisible(true);  // Ensure setVisible
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        }
    }
    // SignupFrame class content
    public static class SignupFrame extends JFrame {
        private JTextField usernameField, fullnameField;
        private JPasswordField passwordField;

        public SignupFrame() {
            setTitle("Sign Up");
            setSize(400, 300);
            setLocationRelativeTo(null);

            JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

            form.add(new JLabel("Full Name:"));
            fullnameField = new JTextField();
            form.add(fullnameField);

            form.add(new JLabel("Username:"));
            usernameField = new JTextField();
            form.add(usernameField);

            form.add(new JLabel("Password:"));
            passwordField = new JPasswordField();
            form.add(passwordField);

            JButton signupBtn = new JButton("Create Account");
            form.add(new JLabel());
            form.add(signupBtn);

            add(form);

            signupBtn.addActionListener(e -> signUp());
            setVisible(true);
        }

        private void signUp() {
            String username = usernameField.getText();
            String fullname = fullnameField.getText();
            String password = new String(passwordField.getPassword());

            try (Connection conn = AuthManager.DBUtil.getConnection()) {
                conn.setAutoCommit(false);
                PreparedStatement ps = conn.prepareStatement("INSERT INTO users(username, password, fullname) VALUES(?, ?, ?)");
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, fullname);
                ps.executeUpdate();
                conn.commit();
                JOptionPane.showMessageDialog(this, "Account created! Please login.");
                dispose();
            } catch (SQLIntegrityConstraintViolationException e) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error creating account: " + ex.getMessage());
            }
        }
    }
    // Main method to start the application
    public static void main(String[] args) {
        // TEST DATABASE CONNECTION FIRST
        System.out.println("🔍 TESTING DATABASE CONNECTION...");
        try {
            Connection conn = DBUtil.getConnection();
            System.out.println("✅ CONNECTION SUCCESS!");
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users");
            ResultSet rs = ps.executeQuery();
            rs.next();
            System.out.println("✅ USERS TABLE HAS " + rs.getInt(1) + " ROWS");
            conn.close();
        } catch (Exception e) {
            System.out.println("❌ CONNECTION FAILED!");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "DATABASE ERROR:\n" + e.getMessage());
            return; // Don't start GUI
        }

        // START GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}