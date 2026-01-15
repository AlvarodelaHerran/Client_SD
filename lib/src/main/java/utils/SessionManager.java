package utils;

public class SessionManager {
    
    private static SessionManager instance;
    private String authToken;
    private String userEmail;
    
    private SessionManager() {
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setUserEmail(String email) {
        this.userEmail = email;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public boolean isAuthenticated() {
        return authToken != null && !authToken.isEmpty();
    }
    
    public void clearSession() {
        authToken = null;
        userEmail = null;
    }
}