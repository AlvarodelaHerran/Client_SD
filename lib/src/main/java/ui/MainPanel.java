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

public class MainPanel extends JPanel {

    private final MainApplicationFrame parentFrame;
    private final DumpsterController dumpsterController;
    private final AuthController authController;
    
    private JTable tableDumpsters;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JLabel lblStatus;
    private List<Dumpster> currentDumpsters;

    public MainPanel(MainApplicationFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        String baseUrl = "http://localhost:8899";
        this.dumpsterController = new DumpsterController(baseUrl);
        this.authController = new AuthController(baseUrl);
        
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 245, 245));
        
        initUI();
        loadDumpsters();
    }

    private void initUI() {
        add(createHeader(), BorderLayout.NORTH);
        add(createDumpstersTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(new Color(66, 133, 244));
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        // Usuario actual
        String userEmail = authController.getCurrentUserEmail();
        JLabel lblUser = new JLabel("👤 " + userEmail);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(Color.WHITE);
        header.add(lblUser, BorderLayout.WEST);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);
        
        btnRefresh = createHeaderButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadDumpsters());
        buttonPanel.add(btnRefresh);
        
        JButton btnManage = createHeaderButton("⚙ Management");
        btnManage.addActionListener(e -> parentFrame.showManagementPanel());
        buttonPanel.add(btnManage);

        JButton btnLogout = createHeaderButton("Log Out");
        btnLogout.addActionListener(e -> handleLogout());
        buttonPanel.add(btnLogout);

        header.add(buttonPanel, BorderLayout.EAST);
        return header;
    }

    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(48, 110, 220));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private JScrollPane createDumpstersTable() {
        String[] columns = {"ID", "Location", "Postal Code", "Capacity", "Fill", "%", "State", "Plant"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        tableDumpsters = new JTable(tableModel);
        tableDumpsters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDumpsters.setRowHeight(32);
        tableDumpsters.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableDumpsters.setGridColor(new Color(220, 220, 220));
        tableDumpsters.setShowGrid(true);
        tableDumpsters.setIntercellSpacing(new Dimension(1, 1));
        tableDumpsters.getTableHeader().setReorderingAllowed(false);
        tableDumpsters.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableDumpsters.getTableHeader().setBackground(new Color(240, 240, 240));

       
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

        setupPlantColumn();
        tableDumpsters.setRowSorter(new TableRowSorter<>(tableModel));

        tableDumpsters.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDumpsterDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableDumpsters);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private void setupPlantColumn() {
        tableDumpsters.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            JButton btn = new JButton("Assign");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                                                           boolean isSelected, boolean hasFocus, 
                                                           int row, int column) {
                if (value instanceof RecyclingPlant plant) {
                    JLabel lbl = new JLabel(plant.getName());
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    return lbl;
                } else {
                    btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    btn.setBackground(new Color(66, 133, 244));
                    btn.setForeground(Color.WHITE);
                    btn.setFocusPainted(false);
                    return btn;
                }
            }
        });

        tableDumpsters.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private JButton btn = new JButton("Assign");
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
                                JOptionPane.showMessageDialog(MainPanel.this, 
                                    "There are no recycling plants available", 
                                    "Info", 
                                    JOptionPane.INFORMATION_MESSAGE);
                                fireEditingStopped();
                                return;
                            }

                            showPlantSelectionDialog(plants, currentDumpster, modelRow);
                        } catch (Exception ex) {
                            showError("Error loading plants", ex);
                        }
                    }
                };
                worker.execute();
            }
        });
    }

    private void showPlantSelectionDialog(List<RecyclingPlant> plants, Dumpster dumpster, int modelRow) {
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
                        MainPanel.this,
                        "Select recycling plant for Dumpster #" + dumpster.getId(),
                        "Assign Plant", 
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
                    showError("Error displaying dialog", ex);
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
                        
                        JOptionPane.showMessageDialog(MainPanel.this, 
                            "Plant correctly assigned",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        loadDumpsters();
                    }
                } catch (Exception ex) {
                    showError("Error assigning plant", ex);
                }
            }
        };
        worker.execute();
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(250, 250, 250));
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        lblStatus = new JLabel("Ready");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(100, 100, 100));
        footer.add(lblStatus, BorderLayout.WEST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        legend.setOpaque(false);
        
        legend.add(createLegendLabel("Low", new Color(76, 175, 80)));
        legend.add(createLegendLabel("Medium", new Color(255, 152, 0)));
        legend.add(createLegendLabel("Full", new Color(244, 67, 54)));

        footer.add(legend, BorderLayout.EAST);
        return footer;
    }

    private JLabel createLegendLabel(String text, Color color) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setOpaque(true);
        lbl.setBackground(color);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return lbl;
    }

    private void loadDumpsters() {
        setButtonsEnabled(false);
        updateStatus("Loading...");

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
                    updateStatus("✓ " + dumpsters.size() + " dumpsters");
                } catch (Exception ex) {
                    showError("Error loading dumpsters", ex);
                    updateStatus("Error");
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
                null,
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
			"Location: %s\n" +
			"Postal Code: %d\n" +
			"Capacity: %d L\n" +
			"Current Fill: %d L\n" +
			"Percentage: %.1f%%\n" +
			"Assigned Floor: %s",
            d.getId(),
            d.getLocation(),
            d.getPostalCode(),
            d.getCapacity(),
            d.getCurrentFill(),
            d.getFillPercentage(),
            d.getAssignedPlant() != null ? d.getAssignedPlant().getName() : "Unassigned"
        );
        
        JOptionPane.showMessageDialog(
            this,
            details,
            "Dumpster Details #" + d.getId(),
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out?",
            "Confirm Logout",
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
                        parentFrame.returnToLogin();
                        
                        if (!result.isSuccess()) {
                            System.err.println("Warning: " + result.getMessage());
                        }
                    } catch (Exception ex) {
                        parentFrame.returnToLogin();
                    }
                }
            };
            worker.execute();
        }
    }


    private void updateStatus(String message) {
        lblStatus.setText(message);
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
        
        ex.printStackTrace();
    }
}
