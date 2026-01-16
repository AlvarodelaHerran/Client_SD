package ui;

import model.Dumpster;
import controller.DumpsterController;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DumpsterManagementPanel extends JPanel {

    private final MainApplicationFrame parentFrame;
    private final DumpsterController controller;
    
    private JTextField txtLocation;
    private JTextField txtPostalCode;
    private JTextField txtCapacity;
    private JTextField txtCurrentFill;
    private JButton btnCreate;
    private JButton btnUpdate;
    private JComboBox<Dumpster> cmbDumpsters;
    private List<Dumpster> dumpsters;

    public DumpsterManagementPanel(MainApplicationFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.controller = new DumpsterController("http://localhost:8899");
        
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        
        initUI();
        loadDumpsters();
    }

    private void initUI() {
        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(66, 133, 244));
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        JLabel lblTitle = new JLabel("⚙ Dumpsters Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);
        
        JButton btnBack = new JButton("← Return");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setForeground(Color.WHITE);
        btnBack.setBackground(new Color(48, 110, 220));
        btnBack.setFocusPainted(false);
        btnBack.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> parentFrame.showPanel(MainApplicationFrame.MAIN_PANEL));
        header.add(btnBack, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createFormPanel() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(new Color(245, 245, 245));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Selector de dumpster
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblSelect = new JLabel("Select Dumpster:");
        lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(lblSelect, gbc);

        gbc.gridx = 1;
        cmbDumpsters = new JComboBox<>();
        cmbDumpsters.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbDumpsters.addActionListener(e -> populateFields());
        formPanel.add(cmbDumpsters, gbc);

        // Separador
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 15, 10);
        JSeparator separator = new JSeparator();
        formPanel.add(separator, gbc);

        // Campos del formulario
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        gbc.gridy++;
        addFormField(formPanel, gbc, "Ubication:", txtLocation = new JTextField());
        
        gbc.gridy++;
        addFormField(formPanel, gbc, "Postal code:", txtPostalCode = new JTextField());
        
        gbc.gridy++;
        addFormField(formPanel, gbc, "Capacity (L):", txtCapacity = new JTextField());
        
        gbc.gridy++;
        addFormField(formPanel, gbc, "Current fill (L):", txtCurrentFill = new JTextField());

        // Botones
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 5, 10);
        
        btnCreate = createButton("Create New Dumpster", new Color(76, 175, 80));
        btnCreate.addActionListener(e -> createDumpster());
        formPanel.add(btnCreate, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(5, 10, 0, 10);
        btnUpdate = createButton("Update Filling", new Color(255, 152, 0));
        btnUpdate.addActionListener(e -> updateDumpster());
        formPanel.add(btnUpdate, gbc);

        
        GridBagConstraints containerGbc = new GridBagConstraints();
        containerGbc.gridx = 0;
        containerGbc.gridy = 0;
        containerGbc.insets = new Insets(20, 20, 20, 20);
        container.add(formPanel, containerGbc);
        
        return container;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField) {
        gbc.gridx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        panel.add(textField, gbc);
    }

    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
                    JOptionPane.showMessageDialog(DumpsterManagementPanel.this,
                            "Error loading: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            
            JOptionPane.showMessageDialog(this, 
                "Dumpster created with ID: " + created.getId(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            loadDumpsters();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numerical values", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error creating: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDumpster() {
        Dumpster selected = (Dumpster) cmbDumpsters.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a dumpster", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int currentFill = Integer.parseInt(txtCurrentFill.getText());
            boolean success = controller.updateDumpsterFill(selected.getId(), currentFill);
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Dumpster updated successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadDumpsters();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Dumpster not found", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid numeric value", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Update error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
