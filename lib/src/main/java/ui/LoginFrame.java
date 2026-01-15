package ui;

import service.AuthServiceClient;
import utils.SessionManager;
import ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private final AuthServiceClient authService;
    private final JTextField txtEmail;
    private final JPasswordField txtPassword;
    private final JButton btnLogin;

    public LoginFrame() {
        super("Ecoembes - Login");

        this.authService = new AuthServiceClient("http://localhost:8899");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 5, 5));

        JLabel lblEmail = new JLabel("Email:");
        txtEmail = new JTextField();

        JLabel lblPassword = new JLabel("Password:");
        txtPassword = new JPasswordField();

        btnLogin = new JButton("Login");

        add(lblEmail);
        add(txtEmail);
        add(lblPassword);
        add(txtPassword);
        add(new JLabel());
        add(btnLogin);

        btnLogin.addActionListener(e -> handleLogin());
        
        txtPassword.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        btnLogin.setEnabled(false);
        btnLogin.setText("Connexion...");

        SwingWorker<Optional<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected Optional<String> doInBackground() throws Exception {
                String email = txtEmail.getText().trim();
                String password = new String(txtPassword.getPassword());
                return authService.login(email, password);
            }

            @Override
            protected void done() {
                try {
                    Optional<String> token = get();

                    if (token.isPresent()) {
                        onLoginSuccess(token.get());
                    } else {
                        onLoginFailure("Identifiants invalides");
                    }
                } catch (Exception ex) {
                    onLoginFailure("Erreur: " + ex.getMessage());
                } finally {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                }
            }
        };

        worker.execute();
    }

    private void onLoginSuccess(String token) {
        SessionManager.getInstance().setAuthToken(token);
        SessionManager.getInstance().setUserEmail(txtEmail.getText().trim());

        txtPassword.setText("");

        openMainWindow();

        dispose();
    }

    private void onLoginFailure(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Erreur de connexion",
            JOptionPane.ERROR_MESSAGE
        );
        
        txtPassword.setText("");
        txtPassword.requestFocus();
    }

    private void openMainWindow() {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
           mainFrame.setVisible(true);
        });
    }
}