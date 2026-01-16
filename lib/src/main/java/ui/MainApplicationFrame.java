package ui;

import javax.swing.*;
import java.awt.*;

public class MainApplicationFrame extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    public static final String LOGIN_PANEL = "LOGIN";
    public static final String MAIN_PANEL = "MAIN";
    public static final String MANAGEMENT_PANEL = "MANAGEMENT";
    
    public MainApplicationFrame() {
        super("Ecoembes - Management System");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 650);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
 
        LoginPanel loginPanel = new LoginPanel(this);
        mainPanel.add(loginPanel, LOGIN_PANEL);
        
        add(mainPanel);
        
        showPanel(LOGIN_PANEL);
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
    

    public void initializeMainPanel() {
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp.getName() != null && comp.getName().equals(MAIN_PANEL)) {
                mainPanel.remove(comp);
            }
        }
        
        MainPanel mainPanelView = new MainPanel(this);
        mainPanelView.setName(MAIN_PANEL);
        mainPanel.add(mainPanelView, MAIN_PANEL);
        showPanel(MAIN_PANEL);
    }
    

    public void showManagementPanel() {
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp.getName() != null && comp.getName().equals(MANAGEMENT_PANEL)) {
                mainPanel.remove(comp);
            }
        }
        
        DumpsterManagementPanel managementPanel = new DumpsterManagementPanel(this);
        managementPanel.setName(MANAGEMENT_PANEL);
        mainPanel.add(managementPanel, MANAGEMENT_PANEL);
        showPanel(MANAGEMENT_PANEL);
    }
    
    public void returnToLogin() {
        Component[] components = mainPanel.getComponents();
        for (Component comp : components) {
            if (comp.getName() != null && !comp.getName().equals(LOGIN_PANEL)) {
                mainPanel.remove(comp);
            }
        }
        
        showPanel(LOGIN_PANEL);
    }
}
