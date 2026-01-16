package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Dumpster;
import model.UsageRecord;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class DumpsterServiceClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public DumpsterServiceClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }

    public List<Dumpster> getAllDumpsters(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/dumpsters"))
                .header("Token", token)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new IllegalStateException("Not authorized - Token invalid");
        }

        if (response.statusCode() == 204) {
            return List.of();
        }

        if (response.statusCode() != 200) {
            throw new IOException("Error retrieving dumpsters:" + response.statusCode());
        }

        Dumpster[] dumpsters = gson.fromJson(response.body(), Dumpster[].class);
        return Arrays.asList(dumpsters);
    }

    public boolean updateDumpsterInfo(long dumpsterId, int currentFill, String token)
            throws IOException, InterruptedException {

        String jsonBody = gson.toJson(currentFill);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/dumpsters/" + dumpsterId + "/dump_info"))
                .header("Token", token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new IllegalStateException("Not authorized - Invalid token");
        }

        return response.statusCode() == 200;
    }

    public Dumpster createDumpster(Dumpster dumpster, String token) throws IOException, InterruptedException {

        String jsonBody = gson.toJson(dumpster);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/dumpsters"))
                .header("Token", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new IllegalStateException("Not authorized - Invalid token");
        }

        if (response.statusCode() != 200) {
            throw new IOException("Error creating dumpster:" + response.statusCode());
        }

        return gson.fromJson(response.body(), Dumpster.class);
    }

    public List<UsageRecord> getDumpsterUsage(long dumpsterId, LocalDate startDate, LocalDate endDate, String token)
            throws IOException, InterruptedException {

        String url = String.format("%s/dumpsters/%d/usage?start_date=%s&end_date=%s",
                baseUrl, dumpsterId, startDate, endDate);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Token", token)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 401) {
            throw new IllegalStateException("Error creating dumpster:");
        }

        if (response.statusCode() == 204) {
            return List.of();
        }

        if (response.statusCode() != 200) {
            throw new IOException("Error retrieving dumpster usage:" + response.statusCode());
        }

        UsageRecord[] records = gson.fromJson(response.body(), UsageRecord[].class);
        return Arrays.asList(records);
    }

    public List<Dumpster> getDumpstersByPostalCodeAndDate(LocalDate date, int postalCode, String token)
            throws IOException, InterruptedException {

        String url = String.format("%s/dumpsters/status/postal_code?date=%s&postal_code=%d",
                baseUrl, date, postalCode);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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
            throw new IOException("Error retrieving dumpsters by postal code: " + response.statusCode());
        }

        Dumpster[] dumpsters = gson.fromJson(response.body(), Dumpster[].class);
        return Arrays.asList(dumpsters);
    }
}
