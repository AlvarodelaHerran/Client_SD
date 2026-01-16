package ui;

import controller.AuthController;
import controller.DumpsterController;
import controller.DumpsterController.ControllerException;
import model.Dumpster;
import model.RecyclingPlant;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Frame principal refactorizado para usar controladores
 */
public class MainFrame extends JFrame {

    private final DumpsterController dumpsterController;
    private final AuthController authController;
    
    private JTable tableDumpsters;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JPanel statusPanel;
    private List<Dumpster> currentDumpsters;

    public MainFrame() {
        super("Ecoembes - Dumpsters List");
        
        // Inicializar controladores
        String baseUrl = "http://localhost:8899";
        this.dumpsterController = new DumpsterController(baseUrl);
        this.authController = new AuthController(baseUrl);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(null);
        
        initUI();
        loadDumpsters();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        add(createHeader(), BorderLayout.NORTH);
        add(createDumpstersTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Usuario actual
        String userEmail = authController.getCurrentUserEmail();
        JLabel lblUser = new JLabel("👤 Conectado: " + userEmail);
        lblUser.setFont(new Font("Arial", Font.BOLD, 12));
        header.add(lblUser, BorderLayout.WEST);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        btnRefresh = new JButton("🔄 Refrescar");
        btnRefresh.addActionListener(e -> loadDumpsters());
        buttonPanel.add(btnRefresh);
        
        JButton btnManage = new JButton("Gestionar Dumpsters");
        btnManage.addActionListener(e -> openManagementFrame());
        buttonPanel.add(btnManage);

        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.addActionListener(e -> handleLogout());
        buttonPanel.add(btnLogout);

        header.add(buttonPanel, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createDumpstersTable() {
        String[] columns = {"ID", "Ubicación", "Código Postal", "Capacidad", "Llenado", "Porcentaje", "Nivel", "Planta"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Solo la columna de planta es editable
            }
        };

        tableDumpsters = new JTable(tableModel);
        tableDumpsters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDumpsters.setRowHeight(30);
        tableDumpsters.getTableHeader().setReorderingAllowed(false);

        // Renderer para la columna de nivel (color)
        tableDumpsters.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, 
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
                if (!isSelected && currentDumpsters != null && row < currentDumpsters.size()) {
                    Dumpster dumpster = currentDumpsters.get(table.convertRowIndexToModel(row));
                    c.setBackground(dumpster.getFillLevelColor());
                }
                setText("");
                return c;
            }
        });

        // Renderer y Editor para la columna de planta
        setupPlantColumn();

        tableDumpsters.setRowSorter(new TableRowSorter<>(tableModel));

        // Doble clic para ver detalles
        tableDumpsters.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDumpsterDetails();
                }
            }
        });

        return new JScrollPane(tableDumpsters);
    }

    private void setupPlantColumn() {
        // Renderer para mostrar planta asignada o botón
        tableDumpsters.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            JButton btn = new JButton("Asignar");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                                                           boolean isSelected, boolean hasFocus, 
                                                           int row, int column) {
                if (value instanceof RecyclingPlant plant) {
                    return new JLabel(plant.getName());
                } else {
                    return btn;
                }
            }
        });

        // Editor para asignar planta
        tableDumpsters.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private JButton btn = new JButton("Asignar");
            private Dumpster currentDumpster;

            {
                btn.addActionListener(e -> handlePlantAssignment());
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, 
                                                          boolean isSelected, int row, int column) {
                return btn;
            }

            @Override
            public Object getCellEditorValue() {
                return currentDumpster != null && currentDumpster.getAssignedPlant() != null
                        ? currentDumpster.getAssignedPlant()
                        : null;
            }

            private void handlePlantAssignment() {
            	int selectedRow = tableDumpsters.getSelectedRow();
                if (selectedRow == -1) return;
                
                // Guardamos el índice convertido en una nueva variable que NO modificaremos después
                // Esto la hace "effectively final"
                int modelRow = tableDumpsters.convertRowIndexToModel(selectedRow);
                
                currentDumpster = currentDumpsters.get(modelRow);

                SwingWorker<List<RecyclingPlant>, Void> worker = new SwingWorker<>() {
                    @Override
                    protected List<RecyclingPlant> doInBackground() throws Exception {
                        return dumpsterController.getAllRecyclingPlants();
                    }

                    @Override
                    protected void done() {
                        try {
                            List<RecyclingPlant> plants = get();
                            
                            if (plants.isEmpty()) {
                                JOptionPane.showMessageDialog(MainFrame.this, 
                                    "No hay plantas de reciclaje disponibles", 
                                    "Info", 
                                    JOptionPane.INFORMATION_MESSAGE);
                                fireEditingStopped();
                                return;
                            }

                            showPlantSelectionDialog(plants, currentDumpster, modelRow);
                        } catch (Exception ex) {
                            showError("Error al cargar plantas", ex);
                        }
                    }
                };
                worker.execute();
            }
        });
    }

    private void showPlantSelectionDialog(List<RecyclingPlant> plants, Dumpster dumpster, int modelRow) {
        // Obtener capacidades de forma asíncrona
        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override
            protected String[] doInBackground() throws Exception {
                LocalDate today = LocalDate.now();
                return plants.stream().map(p -> {
                    try {
                        Optional<Integer> cap = dumpsterController.getPlantCapacity(p.getName(), today);
                        return p.getName() + " — " + cap.map(c -> c + "L").orElse("?");
                    } catch (Exception ex) {
                        return p.getName() + " — ?";
                    }
                }).toArray(String[]::new);
            }

            @Override
            protected void done() {
                try {
                    String[] plantChoices = get();
                    
                    String selectedEntry = (String) JOptionPane.showInputDialog(
                        MainFrame.this,
                        "Seleccione planta de reciclaje para Dumpster #" + dumpster.getId(),
                        "Asignar Planta", 
                        JOptionPane.PLAIN_MESSAGE, 
                        null,
                        plantChoices, 
                        plantChoices[0]
                    );

                    if (selectedEntry != null) {
                        String selectedPlantName = selectedEntry.split(" — ")[0];
                        assignPlantToDumpster(dumpster, selectedPlantName, plants, modelRow);
                    }
                } catch (Exception ex) {
                    showError("Error al mostrar diálogo", ex);
                }
            }
        };
        worker.execute();
    }

    private void assignPlantToDumpster(Dumpster dumpster, String plantName, 
                                       List<RecyclingPlant> plants, int modelRow) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return dumpsterController.assignDumpsterToPlant(dumpster.getId(), plantName);
            }

            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    
                    if (success) {
                        RecyclingPlant selectedPlant = plants.stream()
                            .filter(p -> p.getName().equals(plantName))
                            .findFirst()
                            .orElse(null);

                        if (selectedPlant != null) {
                            dumpster.setAssignedPlant(selectedPlant);
                            tableDumpsters.setValueAt(selectedPlant, modelRow, 7);
                        }
                        
                        JOptionPane.showMessageDialog(MainFrame.this, 
                            "Planta asignada correctamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        loadDumpsters(); // Recargar para mostrar cambios
                    }
                } catch (Exception ex) {
                    showError("Error al asignar planta", ex);
                }
            }
        };
        worker.execute();
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Panel de estado
        statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Listo"));
        footer.add(statusPanel, BorderLayout.WEST);

        // Leyenda de colores
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JLabel lblGreen = new JLabel(" Bajo ");
        lblGreen.setOpaque(true);
        lblGreen.setBackground(new Color(76, 175, 80));
        lblGreen.setForeground(Color.WHITE);
        lblGreen.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        legend.add(lblGreen);

        JLabel lblOrange = new JLabel(" Medio ");
        lblOrange.setOpaque(true);
        lblOrange.setBackground(new Color(255, 152, 0));
        lblOrange.setForeground(Color.WHITE);
        lblOrange.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        legend.add(lblOrange);

        JLabel lblRed = new JLabel(" Lleno ");
        lblRed.setOpaque(true);
        lblRed.setBackground(new Color(244, 67, 54));
        lblRed.setForeground(Color.WHITE);
        lblRed.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        legend.add(lblRed);

        footer.add(legend, BorderLayout.EAST);
        return footer;
    }

    private void loadDumpsters() {
        setButtonsEnabled(false);
        updateStatus("Cargando dumpsters...");

        SwingWorker<List<Dumpster>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Dumpster> doInBackground() throws Exception {
                return dumpsterController.getAllDumpsters();
            }

            @Override
            protected void done() {
                try {
                    List<Dumpster> dumpsters = get();
                    currentDumpsters = dumpsters;
                    displayDumpsters(dumpsters);
                    updateStatus("✓ Cargados " + dumpsters.size() + " dumpsters");
                } catch (Exception ex) {
                    showError("Error al cargar dumpsters", ex);
                    updateStatus("Error al cargar datos");
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void displayDumpsters(List<Dumpster> dumpsters) {
        tableModel.setRowCount(0);
        for (Dumpster d : dumpsters) {
            tableModel.addRow(new Object[]{
                d.getId(),
                d.getLocation(),
                d.getPostalCode(),
                d.getCapacity() + " L",
                d.getCurrentFill() + " L",
                String.format("%.1f%%", d.getFillPercentage()),
                null, // Columna de nivel (se renderiza con color)
                d.getAssignedPlant() != null ? d.getAssignedPlant() : null
            });
        }
    }

    private void showDumpsterDetails() {
        int row = tableDumpsters.getSelectedRow();
        if (row == -1) return;
        
        row = tableDumpsters.convertRowIndexToModel(row);
        Dumpster d = currentDumpsters.get(row);
        
        String details = String.format(
            "ID: %d\n" +
            "Ubicación: %s\n" +
            "Código Postal: %d\n" +
            "Capacidad: %d L\n" +
            "Llenado actual: %d L\n" +
            "Porcentaje: %.1f%%\n" +
            "Planta asignada: %s",
            d.getId(),
            d.getLocation(),
            d.getPostalCode(),
            d.getCapacity(),
            d.getCurrentFill(),
            d.getFillPercentage(),
            d.getAssignedPlant() != null ? d.getAssignedPlant().getName() : "Sin asignar"
        );
        
        JOptionPane.showMessageDialog(
            this,
            details,
            "Detalles del Dumpster #" + d.getId(),
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void openManagementFrame() {
        SwingUtilities.invokeLater(() -> 
            new DumpsterManagementFrame().setVisible(true)
        );
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea cerrar sesión?",
            "Confirmar Logout",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<AuthController.LogoutResult, Void> worker = new SwingWorker<>() {
                @Override
                protected AuthController.LogoutResult doInBackground() {
                    return authController.logout();
                }

                @Override
                protected void done() {
                    try {
                        AuthController.LogoutResult result = get();
                        
                        // Independientemente del resultado, cerrar sesión localmente
                        dispose();
                        SwingUtilities.invokeLater(() -> 
                            new LoginFrame().setVisible(true)
                        );
                        
                        if (!result.isSuccess()) {
                            System.err.println("Advertencia: " + result.getMessage());
                        }
                    } catch (Exception ex) {
                        // Aún así cerrar sesión localmente
                        dispose();
                        SwingUtilities.invokeLater(() -> 
                            new LoginFrame().setVisible(true)
                        );
                    }
                }
            };
            worker.execute();
        }
    }

    private void updateStatus(String message) {
        statusPanel.removeAll();
        statusPanel.add(new JLabel(message));
        statusPanel.revalidate();
        statusPanel.repaint();
    }

    private void setButtonsEnabled(boolean enabled) {
        btnRefresh.setEnabled(enabled);
    }

    private void showError(String message, Exception ex) {
        String errorMessage = message;
        if (ex instanceof ControllerException) {
            errorMessage = ex.getMessage();
        } else {
            errorMessage += ": " + ex.getMessage();
        }
        
        JOptionPane.showMessageDialog(
            this, 
            errorMessage, 
            "Error", 
            JOptionPane.ERROR_MESSAGE
        );
        
        ex.printStackTrace(); // Para debugging
    }
}
