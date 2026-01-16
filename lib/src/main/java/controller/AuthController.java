package controller;

import service.AuthServiceClient;
import utils.SessionManager;

import java.util.Optional;

/**
 * Controlador que maneja la lógica de autenticación
 */
public class AuthController {

    private final AuthServiceClient authService;

    public AuthController(String baseUrl) {
        this.authService = new AuthServiceClient(baseUrl);
    }

    /**
     * Intenta autenticar al usuario con email y contraseña
     * @return LoginResult con el estado de la autenticación
     */
    public LoginResult login(String email, String password) {
        // Validación de entrada
        if (email == null || email.trim().isEmpty()) {
            return LoginResult.failure("El email no puede estar vacío");
        }
        if (password == null || password.isEmpty()) {
            return LoginResult.failure("La contraseña no puede estar vacía");
        }
        if (!isValidEmail(email)) {
            return LoginResult.failure("El formato del email no es válido");
        }

        try {
            Optional<String> token = authService.login(email.trim(), password);
            
            if (token.isPresent()) {
                // Guardar sesión
                SessionManager.getInstance().setAuthToken(token.get());
                SessionManager.getInstance().setUserEmail(email.trim());
                return LoginResult.success(token.get(), email.trim());
            } else {
                return LoginResult.failure("Credenciales inválidas");
            }
        } catch (Exception e) {
            return LoginResult.failure("Error de conexión: " + e.getMessage());
        }
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public LogoutResult logout() {
        try {
            String token = SessionManager.getInstance().getAuthToken();
            if (token == null) {
                return LogoutResult.failure("No hay sesión activa");
            }

            boolean success = authService.logout(token);
            
            if (success) {
                SessionManager.getInstance().clearSession();
                return LogoutResult.success();
            } else {
                return LogoutResult.failure("Error al cerrar sesión en el servidor");
            }
        } catch (Exception e) {
            // Limpiar sesión local incluso si falla la llamada al servidor
            SessionManager.getInstance().clearSession();
            return LogoutResult.failure("Error durante el logout: " + e.getMessage());
        }
    }

    /**
     * Verifica si el usuario tiene una sesión activa
     */
    public boolean hasActiveSession() {
        return SessionManager.getInstance().getAuthToken() != null;
    }

    /**
     * Obtiene el email del usuario actual
     */
    public String getCurrentUserEmail() {
        return SessionManager.getInstance().getUserEmail();
    }

    /**
     * Validación básica de formato de email
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Resultado de la operación de login
     */
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final String token;
        private final String email;

        private LoginResult(boolean success, String message, String token, String email) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.email = email;
        }

        public static LoginResult success(String token, String email) {
            return new LoginResult(true, "Login exitoso", token, email);
        }

        public static LoginResult failure(String message) {
            return new LoginResult(false, message, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getToken() {
            return token;
        }

        public String getEmail() {
            return email;
        }
    }

    /**
     * Resultado de la operación de logout
     */
    public static class LogoutResult {
        private final boolean success;
        private final String message;

        private LogoutResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static LogoutResult success() {
            return new LogoutResult(true, "Sesión cerrada correctamente");
        }

        public static LogoutResult failure(String message) {
            return new LogoutResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}