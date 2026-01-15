package service;
import com.google.gson.Gson;
import java.net.http.*;
import java.net.URI;
import java.util.Optional;
import model.Credentials;

public class AuthServiceClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String baseUrl;

    public AuthServiceClient(String baseUrl) { this.baseUrl = baseUrl; }

    public Optional<String> login(String email, String password) throws Exception {
        String json = gson.toJson(new Credentials(email,password));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/auth/login"))
            .header("Content-Type","application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 ? Optional.of(response.body()) : Optional.empty();
    }

    public boolean logout(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/auth/logout"))
            .header("Token",token)
            .DELETE()
            .build();
        HttpResponse<Void> response = client.send(request,HttpResponse.BodyHandlers.discarding());
        return response.statusCode() == 204;
    }
}
