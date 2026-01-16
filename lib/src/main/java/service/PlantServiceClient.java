package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import model.AssignRequest;
import model.RecyclingPlant;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PlantServiceClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public PlantServiceClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder().create();
    }

    public List<RecyclingPlant> getAllPlants(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/recyclingPlants"))
                .header("Token", token)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new IllegalStateException("Unauthorized - Invalid token");
        }

        if (response.statusCode() == 204) {
            return List.of();
        }

        if (response.statusCode() != 200) {
            throw new IOException("Error during plant retrieval:" + response.statusCode());
        }

        Type listType = new TypeToken<List<RecyclingPlant>>() {}.getType();
        return gson.fromJson(response.body(), listType);
    }
    
    public Integer getPlantCapacity(String token, String plantName, String date)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/recyclingPlants/" + plantName + "/capacity?date=" + date))
                .header("Token", token)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new IllegalStateException("Not authorized - Invalid token");
        }

        if (response.statusCode() == 404) {
            return null; // Le plant n'existe pas
        }

        if (response.statusCode() != 200) {
            throw new IOException("Error retrieving capacity:" + response.statusCode());
        }

        return gson.fromJson(response.body(), Integer.class);
    }

    
    public boolean assignDumpstersToPlant(String token, String plantName, List<Long> dumpsterIds)
            throws IOException, InterruptedException {

        AssignRequest payload = new AssignRequest(plantName, dumpsterIds);
        String jsonBody = gson.toJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/recyclingPlants/assignDumpster"))
                .header("Token", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new IllegalStateException("Unauthorized - invalide token");
        }

        if (response.statusCode() == 400) {
            throw new IllegalArgumentException("incorrect request (bad parameter)");
        }
        if (response.statusCode() != 200) {
            throw new IllegalArgumentException("unexpected error");
        }

        return response.statusCode() == 200;
    }
}
