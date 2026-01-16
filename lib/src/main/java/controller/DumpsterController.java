package controller;

import model.Dumpster;
import model.RecyclingPlant;
import service.DumpsterServiceClient;
import service.PlantServiceClient;
import utils.SessionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador que maneja la lógica de negocio relacionada con dumpsters.
 * Actúa como intermediario entre la UI y los servicios.
 */
public class DumpsterController {

    private final DumpsterServiceClient dumpsterService;
    private final PlantServiceClient plantService;

    public DumpsterController(String baseUrl) {
        this.dumpsterService = new DumpsterServiceClient(baseUrl);
        this.plantService = new PlantServiceClient(baseUrl);
    }

    /**
     * Obtiene todos los dumpsters del usuario autenticado
     */
    public List<Dumpster> getAllDumpsters() throws ControllerException {
        try {
            String token = SessionManager.getInstance().getAuthToken();
            return dumpsterService.getAllDumpsters(token);
        } catch (IllegalStateException e) {
            throw new ControllerException("Sesión no válida. Por favor, vuelva a iniciar sesión.", e);
        } catch (Exception e) {
            throw new ControllerException("Error al cargar los dumpsters: " + e.getMessage(), e);
        }
    }

    /**
     * Crea un nuevo dumpster con validación de datos
     */
    public Dumpster createDumpster(String location, int postalCode, int capacity, int currentFill) 
            throws ControllerException {
        
        // Validación de negocio
        if (location == null || location.trim().isEmpty()) {
            throw new ControllerException("La ubicación no puede estar vacía");
        }
        if (postalCode < 1000 || postalCode > 99999) {
            throw new ControllerException("Código postal inválido");
        }
        if (capacity <= 0) {
            throw new ControllerException("La capacidad debe ser mayor que 0");
        }
        if (currentFill < 0 || currentFill > capacity) {
            throw new ControllerException("El llenado actual debe estar entre 0 y " + capacity);
        }

        try {
            String token = SessionManager.getInstance().getAuthToken();
            Dumpster dumpster = new Dumpster(null, location, postalCode, capacity, currentFill, null, null);
            return dumpsterService.createDumpster(dumpster, token);
        } catch (IllegalStateException e) {
            throw new ControllerException("Sesión no válida", e);
        } catch (Exception e) {
            throw new ControllerException("Error al crear el dumpster: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza el nivel de llenado de un dumpster
     */
    public boolean updateDumpsterFill(long dumpsterId, int currentFill) throws ControllerException {
        if (currentFill < 0) {
            throw new ControllerException("El nivel de llenado no puede ser negativo");
        }

        try {
            String token = SessionManager.getInstance().getAuthToken();
            return dumpsterService.updateDumpsterInfo(dumpsterId, currentFill, token);
        } catch (IllegalStateException e) {
            throw new ControllerException("Sesión no válida", e);
        } catch (Exception e) {
            throw new ControllerException("Error al actualizar el dumpster: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el historial de uso de un dumpster
     */
    public List<model.UsageRecord> getDumpsterUsage(long dumpsterId, LocalDate startDate, LocalDate endDate) 
            throws ControllerException {
        
        if (startDate.isAfter(endDate)) {
            throw new ControllerException("La fecha inicial no puede ser posterior a la fecha final");
        }

        try {
            String token = SessionManager.getInstance().getAuthToken();
            return dumpsterService.getDumpsterUsage(dumpsterId, startDate, endDate, token);
        } catch (Exception e) {
            throw new ControllerException("Error al obtener el historial: " + e.getMessage(), e);
        }
    }

    /**
     * Busca dumpsters por código postal y fecha
     */
    public List<Dumpster> searchDumpstersByPostalCodeAndDate(int postalCode, LocalDate date) 
            throws ControllerException {
        try {
            String token = SessionManager.getInstance().getAuthToken();
            return dumpsterService.getDumpstersByPostalCodeAndDate(date, postalCode, token);
        } catch (Exception e) {
            throw new ControllerException("Error en la búsqueda: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todas las plantas de reciclaje disponibles
     */
    public List<RecyclingPlant> getAllRecyclingPlants() throws ControllerException {
        try {
            String token = SessionManager.getInstance().getAuthToken();
            return plantService.getAllPlants(token);
        } catch (Exception e) {
            throw new ControllerException("Error al cargar las plantas: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene la capacidad disponible de una planta para una fecha
     */
    public Optional<Integer> getPlantCapacity(String plantName, LocalDate date) throws ControllerException {
        try {
            String token = SessionManager.getInstance().getAuthToken();
            Integer capacity = plantService.getPlantCapacity(token, plantName, date.toString());
            return Optional.ofNullable(capacity);
        } catch (Exception e) {
            throw new ControllerException("Error al obtener capacidad: " + e.getMessage(), e);
        }
    }

    /**
     * Asigna un dumpster a una planta de reciclaje
     */
    public boolean assignDumpsterToPlant(long dumpsterId, String plantName) throws ControllerException {
        if (plantName == null || plantName.trim().isEmpty()) {
            throw new ControllerException("Debe seleccionar una planta");
        }

        try {
            String token = SessionManager.getInstance().getAuthToken();
            return plantService.assignDumpstersToPlant(token, plantName, List.of(dumpsterId));
        } catch (IllegalStateException e) {
            throw new ControllerException("Sesión no válida", e);
        } catch (IllegalArgumentException e) {
            throw new ControllerException("Error en la asignación: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ControllerException("Error al asignar planta: " + e.getMessage(), e);
        }
    }

    /**
     * Excepción personalizada para el controlador
     */
    public static class ControllerException extends Exception {
        public ControllerException(String message) {
            super(message);
        }

        public ControllerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}