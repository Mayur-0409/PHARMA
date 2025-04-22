import dao.PharmacyDAO;
import dao.BillGenerator;
import util.DatabaseUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
// import javax.swing.text.html.HTMLEditorKit;
// import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.sql.SQLException;
import java.util.Vector;
import java.awt.Desktop;
import java.io.File;
// import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PharmaCRUDApp extends JFrame {
    private final PharmacyDAO dao;
    private final BillGenerator billGenerator;
    private JComboBox<String> tableSelector;
    private JTable table;
    private DefaultTableModel model;
    private Vector<String> columnNames;

    public PharmaCRUDApp() {
        dao = new PharmacyDAO();
        billGenerator = new BillGenerator();
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("PharmaDB CRUD Application");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableSelector = new JComboBox<>();
        JButton loadBtn = new JButton("Load Table");
        JButton insertBtn = new JButton("Insert");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton generateBillBtn = new JButton("Generate Bill");

        model = new DefaultTableModel();
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Table:"));
        topPanel.add(tableSelector);
        topPanel.add(loadBtn);
        topPanel.add(insertBtn);
        topPanel.add(updateBtn);
        topPanel.add(deleteBtn);
        topPanel.add(generateBillBtn);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadTableNames();

        loadBtn.addActionListener(_ -> loadSelectedTable());
        insertBtn.addActionListener(_ -> insertRow());
        updateBtn.addActionListener(_ -> updateRow());
        deleteBtn.addActionListener(_ -> deleteRow());
        generateBillBtn.addActionListener(_ -> generateBill());
    }

    private void loadTableNames() {
        try {
            Vector<String> tables = dao.getTableNames();
            tableSelector.removeAllItems();
            tables.forEach(tableSelector::addItem);
        } catch (SQLException e) {
            showError("Error loading tables", e);
        }
    }

    private void loadSelectedTable() {
        String selectedTable = (String) tableSelector.getSelectedItem();
        if (selectedTable == null) return;

        try {
            model = dao.getTableData(selectedTable);
            table.setModel(model);
            columnNames = new Vector<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                columnNames.add(model.getColumnName(i));
            }
        } catch (SQLException e) {
            showError("Error loading table data", e);
        }
    }

    private void insertRow() {
        if (columnNames == null || columnNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load a table first");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(columnNames.size() - 1, 2, 5, 5));
        JTextField[] fields = new JTextField[columnNames.size() - 1];
        Vector<String> insertColumnNames = new Vector<>();

        // Skip the first column (ID) and create input fields for other columns
        for (int i = 1; i < columnNames.size(); i++) {
            panel.add(new JLabel(columnNames.get(i) + ":"));
            fields[i - 1] = new JTextField(20);
            panel.add(fields[i - 1]);
            insertColumnNames.add(columnNames.get(i));
        }

        if (JOptionPane.showConfirmDialog(this, panel, "Insert New Record", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            Vector<String> values = new Vector<>();
            for (JTextField field : fields) {
                values.add(field.getText().trim());
            }

            try {
                dao.insertRecord((String) tableSelector.getSelectedItem(), insertColumnNames, values);
                loadSelectedTable();
                JOptionPane.showMessageDialog(this, "Record inserted successfully!");
            } catch (SQLException e) {
                showError("Error inserting record", e);
            }
        }
    }

    private void updateRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(columnNames.size(), 2, 5, 5));
        JTextField[] fields = new JTextField[columnNames.size()];

        for (int i = 0; i < columnNames.size(); i++) {
            panel.add(new JLabel(columnNames.get(i) + ":"));
            fields[i] = new JTextField(model.getValueAt(selectedRow, i).toString());
            panel.add(fields[i]);
        }

        if (JOptionPane.showConfirmDialog(this, panel, "Update Record", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            Vector<String> values = new Vector<>();
            for (JTextField field : fields) {
                values.add(field.getText().trim());
            }

            try {
                dao.updateRecord((String) tableSelector.getSelectedItem(), columnNames, values);
                loadSelectedTable();
                JOptionPane.showMessageDialog(this, "Record updated successfully!");
            } catch (SQLException e) {
                showError("Error updating record", e);
            }
        }
    }

    private void deleteRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this record?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            
            try {
                dao.deleteRecord(
                    (String) tableSelector.getSelectedItem(),
                    columnNames.get(0),
                    model.getValueAt(selectedRow, 0).toString()
                );
                loadSelectedTable();
                JOptionPane.showMessageDialog(this, "Record deleted successfully!");
            } catch (SQLException e) {
                showError("Error deleting record", e);
            }
        }
    }

    private void generateBill() {
        String selectedTable = (String) tableSelector.getSelectedItem();
        if (!"orders".equalsIgnoreCase(selectedTable)) {
            JOptionPane.showMessageDialog(this, 
                "Please select the 'orders' table first", 
                "Generate Bill", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an order to generate the bill", 
                "Generate Bill", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int orderId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            String pdfPath = billGenerator.generateBill(orderId);
            
            // Create bills directory if it doesn't exist
            Files.createDirectories(Paths.get("bills"));
            
            // Open the PDF file
            File pdfFile = new File(pdfPath);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                JOptionPane.showMessageDialog(this,
                    "PDF bill generated at: " + pdfPath,
                    "Bill Generated",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            showError("Error generating bill", e);
        } catch (NumberFormatException e) {
            showError("Invalid Order ID", e);
        } catch (IOException e) {
            showError("Error opening PDF file", e);
        }
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this, 
            message + "\n" + e.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            PharmaCRUDApp app = new PharmaCRUDApp();
            app.setVisible(true);
            Runtime.getRuntime().addShutdownHook(new Thread(DatabaseUtil::closeConnection));
        });
    }
}
