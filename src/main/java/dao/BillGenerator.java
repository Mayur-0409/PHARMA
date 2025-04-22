package dao;

import util.DatabaseUtil;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;

public class BillGenerator {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public String generateBill(int orderId) throws SQLException, IOException {
    String pdfPath = "bills/bill_" + orderId + ".pdf";

    // Ensure 'bills' directory exists
    java.io.File dir = new java.io.File("bills");
    if (!dir.exists()) {
        dir.mkdirs();
    }

    try (PdfWriter writer = new PdfWriter(pdfPath);
         PdfDocument pdf = new PdfDocument(writer);
         Document document = new Document(pdf);
         Connection conn = DatabaseUtil.getConnection()) {

        // Add header
        document.add(new Paragraph("PharmaDB Bill")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(20));

        document.add(new Paragraph("Date: " + DATE_FORMAT.format(new Date()))
            .setTextAlignment(TextAlignment.RIGHT));

        // Get order details
        String orderQuery = "SELECT o.Order_ID, c.Name as CustomerName, " +
                            "c.Phone as CustomerPhone " +
                            "FROM orders o " +
                            "JOIN customer c ON o.Customer_ID = c.Customer_ID " +
                            "WHERE o.Order_ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(orderQuery)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String customerName = rs.getString("CustomerName");
                String phone = rs.getString("CustomerPhone");

                // Add customer details
                document.add(new Paragraph("Customer Name: " + customerName));
                document.add(new Paragraph("Contact: " + phone));
                document.add(new Paragraph("Order ID: " + orderId));
                document.add(new Paragraph("\n"));

                // Create table for order items
                Table table = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25}));
                table.setWidth(UnitValue.createPercentValue(100));

                // Add table headers
                table.addHeaderCell(new Cell().add(new Paragraph("Product")));
                table.addHeaderCell(new Cell().add(new Paragraph("Quantity")));
                table.addHeaderCell(new Cell().add(new Paragraph("Subtotal")));

                // Get order items
                String itemsQuery = "SELECT p.Name as ProductName, od.Quantity, " +
                                    "od.PriceAtPurchase, od.Subtotal " +
                                    "FROM orderdetails od " +
                                    "JOIN product p ON od.Product_ID = p.Product_ID " +
                                    "WHERE od.Order_ID = ?";

                double total = 0.0;
                try (PreparedStatement itemsStmt = conn.prepareStatement(itemsQuery)) {
                    itemsStmt.setInt(1, orderId);
                    ResultSet itemsRs = itemsStmt.executeQuery();

                    while (itemsRs.next()) {
                        String productName = itemsRs.getString("ProductName");
                        int quantity = itemsRs.getInt("Quantity");
                        double subtotal = itemsRs.getDouble("Subtotal");
                        total += subtotal;

                        // Add table row
                        table.addCell(new Cell().add(new Paragraph(productName)));
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(quantity))));
                        table.addCell(new Cell().add(new Paragraph(String.format("%.2f", subtotal))));
                    }
                }

                // Add table to document
                document.add(table);

                // Add total
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("Total Amount: $" + String.format("%.2f", total))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBold());

                // Add footer
                document.add(new Paragraph("\n\nThank you for your business!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic());

            } else {
                throw new SQLException("Order not found: " + orderId);
            }
        }
    }

    return pdfPath;
}

} 