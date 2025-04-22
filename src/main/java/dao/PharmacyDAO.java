package dao;

import util.DatabaseUtil;
import util.ValidationUtil;
import java.sql.*;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class PharmacyDAO {
    public Vector<String> getTableNames() throws SQLException {
        Vector<String> tables = new Vector<>();
        try (Connection conn = DatabaseUtil.getConnection();
             ResultSet rs = conn.getMetaData().getTables("pharmsdb", null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    public DefaultTableModel getTableData(String tableName) throws SQLException {
        Vector<String> columnNames = new Vector<>();
        Vector<Vector<Object>> data = new Vector<>();

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();

            for (int i = 1; i <= cols; i++) {
                columnNames.add(rsmd.getColumnName(i));
            }

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= cols; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }
        }

        return new DefaultTableModel(data, columnNames);
    }

    public void insertRecord(String tableName, Vector<String> columnNames, Vector<String> values) throws SQLException {
        // Validate the data before insertion
        String validationError = null;
        if (tableName.equalsIgnoreCase("customer")) {
            validationError = ValidationUtil.validateCustomerData(columnNames, values);
        } else if (tableName.equalsIgnoreCase("product")) {
            validationError = ValidationUtil.validateProductData(columnNames, values);
        }
        
        if (validationError != null) {
            throw new SQLException(validationError);
        }

        StringBuilder query = new StringBuilder("INSERT INTO " + tableName + " (");
        query.append(String.join(",", columnNames));
        query.append(") VALUES (");
        for (int i = 0; i < columnNames.size(); i++) {
            query.append(i < columnNames.size() - 1 ? "?," : "?");
        }
        query.append(")");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setString(i + 1, values.get(i));
            }
            pstmt.executeUpdate();
        }
    }

    public void updateRecord(String tableName, Vector<String> columnNames, Vector<String> values) throws SQLException {
        StringBuilder query = new StringBuilder("UPDATE " + tableName + " SET ");
        for (int i = 1; i < columnNames.size(); i++) {
            query.append(columnNames.get(i)).append("=?");
            if (i < columnNames.size() - 1) query.append(", ");
        }
        query.append(" WHERE ").append(columnNames.get(0)).append("=?");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
            for (int i = 1; i < values.size(); i++) {
                pstmt.setString(i, values.get(i));
            }
            pstmt.setString(values.size(), values.get(0));
            pstmt.executeUpdate();
        }
    }

    public void deleteRecord(String tableName, String primaryKeyColumn, String primaryKeyValue) throws SQLException {
        String query = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + "=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, primaryKeyValue);
            pstmt.executeUpdate();
        }
    }
} 