package model;

import com.google.gson.annotations.SerializedName;
import java.awt.Color;

public class Dumpster {

    private Long id;

    @SerializedName("address")
    private String location;

    @SerializedName("postalCode")
    private Integer postalCode;

    private Integer capacity;

    @SerializedName("currentFill")
    private Integer currentFill;

    @SerializedName("fillLevel")
    private String fillLevel;

    @SerializedName("assignedPlant")
    private RecyclingPlant assignedPlant;

    // Constructeurs
    public Dumpster() {}

    public Dumpster(Long id, String location, Integer postalCode, Integer capacity,
                    Integer currentFill, String fillLevel, RecyclingPlant assignedPlant) {
        this.id = id;
        this.location = location;
        this.postalCode = postalCode;
        this.capacity = capacity;
        this.currentFill = currentFill;
        this.fillLevel = fillLevel;
        this.assignedPlant = assignedPlant;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getPostalCode() { return postalCode; }
    public void setPostalCode(Integer postalCode) { this.postalCode = postalCode; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getCurrentFill() { return currentFill; }
    public void setCurrentFill(Integer currentFill) { this.currentFill = currentFill; }

    public String getFillLevel() { return fillLevel; }
    public void setFillLevel(String fillLevel) { this.fillLevel = fillLevel; }

    public RecyclingPlant getAssignedPlant() { return assignedPlant; }
    public void setAssignedPlant(RecyclingPlant assignedPlant) { this.assignedPlant = assignedPlant; }

    public double getFillPercentage() {
        if (capacity == null || capacity == 0) return 0;
        return (currentFill != null ? currentFill : 0) * 100.0 / capacity;
    }

    public Color getFillLevelColor() {
        if (fillLevel == null) return Color.GRAY;
        return switch (fillLevel.toUpperCase()) {
            case "GREEN" -> new Color(76, 175, 80);
            case "ORANGE" -> new Color(255, 152, 0);
            case "RED" -> new Color(244, 67, 54);
            default -> Color.GRAY;
        };
    }

    public String getFillLevelDisplay() {
        if (fillLevel == null) return "Inconnu";
        return switch (fillLevel.toUpperCase()) {
            case "GREEN" -> "🟢 Faible";
            case "ORANGE" -> "🟠 Moyen";
            case "RED" -> "🔴 Plein";
            default -> "⚪ Inconnu";
        };
    }

    @Override
    public String toString() {
        return "Dumpster{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", postalCode=" + postalCode +
                ", capacity=" + capacity +
                ", currentFill=" + currentFill +
                ", fillLevel='" + fillLevel + '\'' +
                ", assignedPlant='" + assignedPlant + '\'' +
                '}';
    }
}
