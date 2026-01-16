package ui;

import controller.AuthController;
import controller.AuthController.LoginResult;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

	private final AuthController authController;
	private final MainApplicationFrame parentFrame;
	private JTextField txtEmail;  
	private JPasswordField txtPassword;  
	private JButton btnLogin;  


    public LoginPanel(MainApplicationFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.authController = new AuthController("http://localhost:8899");
        
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 245));
        
        initUI();
    }

    private void initUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(8, 5, 8, 5);
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        
        
        JLabel lblTitle = new JLabel("Login");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(60, 60, 60));
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        formGbc.gridwidth = 2;
        formGbc.insets = new Insets(0, 0, 20, 0);
        formPanel.add(lblTitle, formGbc);
        
        // Email
        formGbc.gridwidth = 1;
        formGbc.gridy = 1;
        formGbc.insets = new Insets(8, 5, 8, 5);
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblEmail, formGbc);
        
        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        txtEmail = new JTextField(20);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        formPanel.add(txtEmail, formGbc);
        
       
        formGbc.gridx = 0;
        formGbc.gridy = 2;
        formGbc.weightx = 0;
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblPassword, formGbc);
        
        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtPassword.addActionListener(e -> handleLogin());
        formPanel.add(txtPassword, formGbc);
        
       
        formGbc.gridx = 0;
        formGbc.gridy = 3;
        formGbc.gridwidth = 2;
        formGbc.insets = new Insets(20, 5, 0, 5);
        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(new Color(66, 133, 244));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> handleLogin());
        formPanel.add(btnLogin, formGbc);
        
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(formPanel, gbc);
    }

    private void handleLogin() {
        btnLogin.setEnabled(false);
        btnLogin.setText("Connecting...");

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
                    onLoginFailure("Unexpected Error: " + ex.getMessage());
                } finally {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Log in");
                }
            }
        };

        worker.execute();
    }

    private void onLoginSuccess() {
        txtPassword.setText("");
        parentFrame.initializeMainPanel();
    }

    private void onLoginFailure(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error in conexion",
            JOptionPane.ERROR_MESSAGE
        );
        
        txtPassword.setText("");
        txtPassword.requestFocus();
    }
}
