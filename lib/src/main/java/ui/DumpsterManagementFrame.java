package ui;

import model.Dumpster;
import controller.DumpsterController;
import controller.DumpsterController.ControllerException;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DumpsterManagementFrame extends JFrame {

	private final DumpsterController controller;
    private JTextField txtLocation;
    private JTextField txtPostalCode;
    private JTextField txtCapacity;
    private JTextField txtCurrentFill;
    private JButton btnCreate;
    private JButton btnUpdate;
    private JComboBox<Dumpster> cmbDumpsters;
    private List<Dumpster> dumpsters;

    public DumpsterManagementFrame() {
        super("Dumpster Management");
        this.controller = new DumpsterController("http://localhost:8899");
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        loadDumpsters();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Select Dumpster:"), gbc);

        gbc.gridx = 1;
        cmbDumpsters = new JComboBox<>();
        cmbDumpsters.addActionListener(e -> populateFields());
        panel.add(cmbDumpsters, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        txtLocation = new JTextField();
        panel.add(txtLocation, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Postal Code:"), gbc);
        gbc.gridx = 1;
        txtPostalCode = new JTextField();
        panel.add(txtPostalCode, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Capacity (L):"), gbc);
        gbc.gridx = 1;
        txtCapacity = new JTextField();
        panel.add(txtCapacity, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Current Fill (L):"), gbc);
        gbc.gridx = 1;
        txtCurrentFill = new JTextField();
        panel.add(txtCurrentFill, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        btnCreate = new JButton("Create Dumpster");
        btnCreate.addActionListener(e -> createDumpster());
        gbc.gridwidth = 2;
        panel.add(btnCreate, gbc);

        gbc.gridy++;
        btnUpdate = new JButton("Update Current Fill");
        btnUpdate.addActionListener(e -> updateDumpster());
        panel.add(btnUpdate, gbc);

        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadDumpsters() {
        SwingWorker<List<Dumpster>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Dumpster> doInBackground() throws Exception {
                return controller.getAllDumpsters();
            }

            @Override
            protected void done() {
                try {
                    dumpsters = get();
                    cmbDumpsters.removeAllItems();
                    for (Dumpster d : dumpsters) {
                        cmbDumpsters.addItem(d);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DumpsterManagementFrame.this,
                            "Erreur lors du chargement: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateFields() {
        Dumpster selected = (Dumpster) cmbDumpsters.getSelectedItem();
        if (selected != null) {
            txtLocation.setText(selected.getLocation());
            txtPostalCode.setText(selected.getPostalCode() != null ? selected.getPostalCode().toString() : "");
            txtCapacity.setText(selected.getCapacity() != null ? selected.getCapacity().toString() : "");
            txtCurrentFill.setText(selected.getCurrentFill() != null ? selected.getCurrentFill().toString() : "");
        }
    }

    private void createDumpster() {
        try {
            String location = txtLocation.getText();
            int postalCode = Integer.parseInt(txtPostalCode.getText());
            int capacity = Integer.parseInt(txtCapacity.getText());
            int currentFill = Integer.parseInt(txtCurrentFill.getText());

            Dumpster created = controller.createDumpster(location, postalCode, capacity, currentFill);
            
            JOptionPane.showMessageDialog(this, "Dumpster created with ID: " + created.getId());
            loadDumpsters();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to create dumpster: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDumpster() {
        Dumpster selected = (Dumpster) cmbDumpsters.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a dumpster", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int currentFill = Integer.parseInt(txtCurrentFill.getText());
            boolean success = controller.updateDumpsterFill(selected.getId(), currentFill);
            if (success) {
                JOptionPane.showMessageDialog(this, "Dumpster updated successfully");
                loadDumpsters();
            } else {
                JOptionPane.showMessageDialog(this, "Dumpster not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to update dumpster: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
