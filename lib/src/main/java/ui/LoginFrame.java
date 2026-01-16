package ui;

import controller.AuthController;
import controller.AuthController.LoginResult;
import utils.SessionManager;
import ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class LoginFrame extends JFrame {

	private final AuthController authController;
    private final JTextField txtEmail;
    private final JPasswordField txtPassword;
    private final JButton btnLogin;

    public LoginFrame() {
        super("Ecoembes - Login");

        this.authController = new AuthController("http://localhost:8899");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 5, 5));

        JLabel lblEmail = new JLabel("Email:", SwingConstants.CENTER);
        txtEmail = new JTextField();

        JLabel lblPassword = new JLabel("Password:", SwingConstants.CENTER);
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

        SwingWorker<LoginResult, Void> worker = new SwingWorker<>() {
            @Override
            protected LoginResult doInBackground() throws Exception {
                String email = txtEmail.getText().trim();
                String password = new String(txtPassword.getPassword());
                return authController.login(email, password);
            }

            @Override
            protected void done() {
                try {
                	LoginResult result = get();

                    if (result.isSuccess()) {
                        onLoginSuccess();
                    } else {
                        onLoginFailure(result.getMessage());
                    }
                } catch (Exception ex) {
                	onLoginFailure("Error inesperado: " + ex.getMessage());
                } finally {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                }
            }
        };

        worker.execute();
    }

    private void onLoginSuccess() {
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