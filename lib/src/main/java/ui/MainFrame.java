package ui;

import model.Dumpster;
import model.RecyclingPlant;
import service.DumpsterServiceClient;
import service.PlantServiceClient;
import utils.SessionManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private final DumpsterServiceClient dumpsterService;
    private final PlantServiceClient plantService;
    private JTable tableDumpsters;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;
    private JPanel statusPanel;
    private List<Dumpster> currentDumpsters;

    public MainFrame() {
        super("Ecoembes - Dumpsters List");
        this.dumpsterService = new DumpsterServiceClient("http://localhost:8899");
        this.plantService = new PlantServiceClient("http://localhost:8899");
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
        String userEmail = SessionManager.getInstance().getUserEmail();
        JLabel lblUser = new JLabel("👤 Connected: " + userEmail);
        lblUser.setFont(new Font("Arial", Font.BOLD, 12));
        header.add(lblUser, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadDumpsters());
        buttonPanel.add(btnRefresh);
        
        JButton btnManage = new JButton("Manage Dumpsters");
        btnManage.addActionListener(e -> new DumpsterManagementFrame().setVisible(true));
        buttonPanel.add(btnManage);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> logout());
        buttonPanel.add(btnLogout);

        header.add(buttonPanel, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createDumpstersTable() {
        String[] columns = {"ID", "Location", "Postal Code", "Capacity", "Filled", "Percentage", "Level", "Recycle Plant"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        tableDumpsters = new JTable(tableModel);
        tableDumpsters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDumpsters.setRowHeight(30);
        tableDumpsters.getTableHeader().setReorderingAllowed(false);

        tableDumpsters.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
                if (!isSelected && currentDumpsters != null && row < currentDumpsters.size()) {
                    Dumpster dumpster = currentDumpsters.get(table.convertRowIndexToModel(row));
                    c.setBackground(dumpster.getFillLevelColor());
                }
                setText("");
                return c;
            }
        });

        tableDumpsters.getColumnModel().getColumn(7).setCellRenderer(new TableCellRenderer() {
            JButton btn = new JButton("Assign");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                if (value instanceof RecyclingPlant plant) {
                    return new JLabel(plant.getName());
                } else {
                    return btn;
                }
            }
        });

        tableDumpsters.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private JButton btn = new JButton("Assign");
            private Dumpster currentDumpster;

            {
                btn.addActionListener(e -> {
                    int row = tableDumpsters.getSelectedRow();
                    if (row == -1) return;
                    row = tableDumpsters.convertRowIndexToModel(row);
                    currentDumpster = currentDumpsters.get(row);

                    try {
                        List<RecyclingPlant> plants = plantService.getAllPlants(SessionManager.getInstance().getAuthToken());
                        if (plants.isEmpty()) {
                            JOptionPane.showMessageDialog(MainFrame.this, "No recycling plants available", "Info", JOptionPane.INFORMATION_MESSAGE);
                            fireEditingStopped();
                            return;
                        }

                        var token = SessionManager.getInstance().getAuthToken();
                        var date = java.time.LocalDate.now().toString();

                        String[] plantChoices = plants.stream().map(p -> {
                            try {
                                Integer cap = plantService.getPlantCapacity(token, p.getName(), date);
                                return p.getName() + " — " + cap + "L";
                            } catch (Exception ex) {
                                return p.getName() + " — ?";
                            }
                        }).toArray(String[]::new);

                        String selectedEntry = (String) JOptionPane.showInputDialog(MainFrame.this,
                                "Select Recycling Plant for Dumpster #" + currentDumpster.getId(),
                                "Assign Plant", JOptionPane.PLAIN_MESSAGE, null,
                                plantChoices, plantChoices[0]);

                        if (selectedEntry != null) {
                            String selectedPlantName = selectedEntry.split(" — ")[0];
                            RecyclingPlant selectedPlant = plants.stream()
                                    .filter(p -> p.getName().equals(selectedPlantName))
                                    .findFirst().orElse(null);

                            if (selectedPlant != null) {
                                plantService.assignDumpstersToPlant(token, selectedPlantName, List.of(currentDumpster.getId()));
                                currentDumpster.setAssignedPlant(selectedPlant);
                                tableDumpsters.setValueAt(selectedPlant, row, 7);
                                loadDumpsters();
                            }
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Failed to assign dumpster: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    fireEditingStopped();
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                return btn;
            }

            @Override
            public Object getCellEditorValue() {
                return currentDumpster != null && currentDumpster.getAssignedPlant() != null
                        ? currentDumpster.getAssignedPlant()
                        : null;
            }
        });

        tableDumpsters.setRowSorter(new TableRowSorter<>(tableModel));

        tableDumpsters.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) showDumpsterDetails();
            }
        });

        return new JScrollPane(tableDumpsters);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Ready"));
        footer.add(statusPanel, BorderLayout.WEST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel lblGreen = new JLabel(" Low ");
        lblGreen.setOpaque(true);
        lblGreen.setBackground(new Color(76, 175, 80));
        lblGreen.setForeground(Color.WHITE);
        lblGreen.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        legend.add(lblGreen);

        JLabel lblOrange = new JLabel(" Medium ");
        lblOrange.setOpaque(true);
        lblOrange.setBackground(new Color(255, 152, 0));
        lblOrange.setForeground(Color.WHITE);
        lblOrange.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        legend.add(lblOrange);

        JLabel lblRed = new JLabel(" Full ");
        lblRed.setOpaque(true);
        lblRed.setBackground(new Color(244, 67, 54));
        lblRed.setForeground(Color.WHITE);
        lblRed.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        legend.add(lblRed);

        footer.add(legend, BorderLayout.EAST);
        return footer;
    }

    private void loadDumpsters() {
        btnRefresh.setEnabled(false);
        statusPanel.removeAll();
        statusPanel.add(new JLabel("Loading dumpsters..."));
        statusPanel.revalidate();
        statusPanel.repaint();

        SwingWorker<List<Dumpster>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Dumpster> doInBackground() throws Exception {
                return dumpsterService.getAllDumpsters(SessionManager.getInstance().getAuthToken());
            }

            @Override
            protected void done() {
                try {
                    List<Dumpster> dumpsters = get();
                    currentDumpsters = dumpsters;
                    displayDumpsters(dumpsters);
                    statusPanel.removeAll();
                    statusPanel.add(new JLabel("✓ Loaded " + dumpsters.size() + " dumpsters"));
                    statusPanel.revalidate();
                    statusPanel.repaint();
                } catch (Exception ex) {
                    handleError(ex);
                } finally {
                    btnRefresh.setEnabled(true);
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
        JOptionPane.showMessageDialog(this,
                "ID: " + d.getId() + "\nLocation: " + d.getLocation(),
                "Dumpster #" + d.getId(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleError(Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void logout() {
        SessionManager.getInstance().clearSession();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
