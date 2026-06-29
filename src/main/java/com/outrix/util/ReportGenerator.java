package com.outrix.util;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.outrix.model.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * PDF Report generator using iText 7.
 */
public class ReportGenerator {

    private static final DeviceRgb PRIMARY_BLUE = new DeviceRgb(29, 78, 216); // #1D4ED8
    private static final DeviceRgb TEXT_DARK    = new DeviceRgb(31, 35, 40); // #1F2328
    private static final DeviceRgb BG_LIGHT     = new DeviceRgb(243, 244, 246); // #F3F4F6
    private static final DeviceRgb WHITE        = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb GRAY_BORDER  = new DeviceRgb(209, 213, 219); // #D1D5DB

    private ReportGenerator() {}

    private static Document createBaseDoc(File file, String title) throws IOException {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);
        Document doc = new Document(pdf);
        doc.setMargins(36, 36, 36, 36);

        // Header band
        Table header = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
        header.setBackgroundColor(PRIMARY_BLUE);
        header.setPadding(10);

        Cell titleCell = new Cell().add(new Paragraph("📦 OUTRIX ERP SYSTEM")
                .setFontSize(18)
                .setBold()
                .setFontColor(WHITE));
        titleCell.setBorder(null);
        titleCell.setPadding(10);
        header.addCell(titleCell);

        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        Cell dateCell = new Cell().add(new Paragraph("Generated:\n" + dateStr)
                .setFontSize(10)
                .setFontColor(WHITE)
                .setTextAlignment(TextAlignment.RIGHT));
        dateCell.setBorder(null);
        dateCell.setPadding(10);
        header.addCell(dateCell);

        doc.add(header);
        doc.add(new Paragraph("\n"));

        // Report Title
        doc.add(new Paragraph(title)
                .setFontSize(16)
                .setBold()
                .setFontColor(PRIMARY_BLUE)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("\n"));

        return doc;
    }

    private static Table createStyledTable(int columns) {
        Table table = new Table(columns).useAllAvailableWidth();
        table.setMarginTop(10);
        table.setMarginBottom(10);
        return table;
    }

    private static Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold().setFontColor(WHITE).setFontSize(10))
                .setBackgroundColor(PRIMARY_BLUE)
                .setBorder(new SolidBorder(GRAY_BORDER, 1))
                .setPadding(6);
    }

    private static Cell createRowCell(String text, boolean altRow) {
        Cell cell = new Cell().add(new Paragraph(text).setFontSize(9).setFontColor(TEXT_DARK))
                .setBorder(new SolidBorder(GRAY_BORDER, 1))
                .setPadding(5);
        if (altRow) {
            cell.setBackgroundColor(BG_LIGHT);
        }
        return cell;
    }

    // ── Sales Report ─────────────────────────────────────────────────────────

    public static void generateSalesReport(File file, String from, String to, List<Sale> sales) throws IOException {
        try (Document doc = createBaseDoc(file, "Sales Analysis Report")) {
            doc.add(new Paragraph("Filter Period: " + from + " to " + to).setFontSize(10).setFontColor(TEXT_DARK));

            // Summary metrics
            BigDecimal totalSales = BigDecimal.ZERO;
            BigDecimal totalDiscounts = BigDecimal.ZERO;
            for (Sale s : sales) {
                totalSales = totalSales.add(s.getGrandTotal());
                totalDiscounts = totalDiscounts.add(s.getDiscount());
            }

            Table summary = new Table(3).useAllAvailableWidth();
            summary.setBackgroundColor(BG_LIGHT);
            summary.setMarginBottom(15);
            summary.addCell(new Cell().add(new Paragraph("Total Transactions\n" + sales.size()).setBold().setTextAlignment(TextAlignment.CENTER)).setPadding(8).setBorder(null));
            summary.addCell(new Cell().add(new Paragraph("Total Discounts Given\n$" + String.format("%.2f", totalDiscounts)).setBold().setTextAlignment(TextAlignment.CENTER)).setPadding(8).setBorder(null));
            summary.addCell(new Cell().add(new Paragraph("Total Revenue\n$" + String.format("%.2f", totalSales)).setBold().setTextAlignment(TextAlignment.CENTER)).setPadding(8).setBorder(null));
            doc.add(summary);

            // Table
            Table table = createStyledTable(6);
            table.addHeaderCell(createHeaderCell("Invoice #"));
            table.addHeaderCell(createHeaderCell("Customer"));
            table.addHeaderCell(createHeaderCell("Cashier"));
            table.addHeaderCell(createHeaderCell("Subtotal"));
            table.addHeaderCell(createHeaderCell("Discount"));
            table.addHeaderCell(createHeaderCell("Grand Total"));

            boolean alt = false;
            for (Sale s : sales) {
                table.addCell(createRowCell(s.getInvoiceNumber(), alt));
                table.addCell(createRowCell(s.getCustomerName() != null ? s.getCustomerName() : "Walk-in", alt));
                table.addCell(createRowCell(s.getUsername() != null ? s.getUsername() : "", alt));
                table.addCell(createRowCell("$" + String.format("%.2f", s.getTotalAmount()), alt));
                table.addCell(createRowCell("$" + String.format("%.2f", s.getDiscount()), alt));
                table.addCell(createRowCell("$" + String.format("%.2f", s.getGrandTotal()), alt));
                alt = !alt;
            }
            doc.add(table);
        }
    }

    // ── Inventory Report ─────────────────────────────────────────────────────

    public static void generateInventoryReport(File file, List<Product> products) throws IOException {
        try (Document doc = createBaseDoc(file, "Stock Inventory Valuation Report")) {
            
            // Summary metrics
            int totalItems = 0;
            int lowStockCount = 0;
            BigDecimal totalValuation = BigDecimal.ZERO;
            for (Product p : products) {
                totalItems += p.getQuantity();
                totalValuation = totalValuation.add(p.getPurchasePrice().multiply(BigDecimal.valueOf(p.getQuantity())));
                if (p.isLowStock() || p.isOutOfStock()) {
                    lowStockCount++;
                }
            }

            Table summary = new Table(3).useAllAvailableWidth();
            summary.setBackgroundColor(BG_LIGHT);
            summary.setMarginBottom(15);
            summary.addCell(new Cell().add(new Paragraph("Total Stock Items\n" + totalItems).setBold().setTextAlignment(TextAlignment.CENTER)).setPadding(8).setBorder(null));
            summary.addCell(new Cell().add(new Paragraph("Low/Out Stock Alerts\n" + lowStockCount).setBold().setTextAlignment(TextAlignment.CENTER)).setPadding(8).setBorder(null));
            summary.addCell(new Cell().add(new Paragraph("Inventory Asset Value\n$" + String.format("%.2f", totalValuation)).setBold().setTextAlignment(TextAlignment.CENTER)).setPadding(8).setBorder(null));
            doc.add(summary);

            // Table
            Table table = createStyledTable(6);
            table.addHeaderCell(createHeaderCell("Product ID"));
            table.addHeaderCell(createHeaderCell("Name"));
            table.addHeaderCell(createHeaderCell("Category"));
            table.addHeaderCell(createHeaderCell("Quantity"));
            table.addHeaderCell(createHeaderCell("Purchase Price"));
            table.addHeaderCell(createHeaderCell("Status"));

            boolean alt = false;
            for (Product p : products) {
                String status = p.isOutOfStock() ? "OUT OF STOCK" : p.isLowStock() ? "LOW STOCK" : "In Stock";
                table.addCell(createRowCell(String.valueOf(p.getId()), alt));
                table.addCell(createRowCell(p.getProductName(), alt));
                table.addCell(createRowCell(p.getCategoryName(), alt));
                table.addCell(createRowCell(String.valueOf(p.getQuantity()), alt));
                table.addCell(createRowCell("$" + String.format("%.2f", p.getPurchasePrice()), alt));
                table.addCell(createRowCell(status, alt));
                alt = !alt;
            }
            doc.add(table);
        }
    }

    // ── Supplier Report ──────────────────────────────────────────────────────

    public static void generateSupplierReport(File file, List<Supplier> suppliers) throws IOException {
        try (Document doc = createBaseDoc(file, "Active Suppliers Directory")) {
            Table table = createStyledTable(5);
            table.addHeaderCell(createHeaderCell("ID"));
            table.addHeaderCell(createHeaderCell("Supplier Name"));
            table.addHeaderCell(createHeaderCell("Contact Phone"));
            table.addHeaderCell(createHeaderCell("Email Address"));
            table.addHeaderCell(createHeaderCell("Physical Address"));

            boolean alt = false;
            for (Supplier s : suppliers) {
                table.addCell(createRowCell(String.valueOf(s.getId()), alt));
                table.addCell(createRowCell(s.getName(), alt));
                table.addCell(createRowCell(s.getContactNumber() != null ? s.getContactNumber() : "–", alt));
                table.addCell(createRowCell(s.getEmail() != null ? s.getEmail() : "–", alt));
                table.addCell(createRowCell(s.getAddress() != null ? s.getAddress() : "–", alt));
                alt = !alt;
            }
            doc.add(table);
        }
    }

    // ── Employee Report ──────────────────────────────────────────────────────

    public static void generateEmployeeReport(File file, List<Employee> employees) throws IOException {
        try (Document doc = createBaseDoc(file, "ERP Employee Roster")) {
            Table table = createStyledTable(6);
            table.addHeaderCell(createHeaderCell("ID"));
            table.addHeaderCell(createHeaderCell("Full Name"));
            table.addHeaderCell(createHeaderCell("System Username"));
            table.addHeaderCell(createHeaderCell("Email"));
            table.addHeaderCell(createHeaderCell("Role"));
            table.addHeaderCell(createHeaderCell("Hire Date"));

            boolean alt = false;
            for (Employee e : employees) {
                table.addCell(createRowCell(String.valueOf(e.getId()), alt));
                table.addCell(createRowCell(e.getName(), alt));
                table.addCell(createRowCell(e.getUsername() != null ? e.getUsername() : "–", alt));
                table.addCell(createRowCell(e.getEmail() != null ? e.getEmail() : "–", alt));
                table.addCell(createRowCell(e.getRole() != null ? e.getRole() : "–", alt));
                table.addCell(createRowCell(e.getHireDate() != null ? e.getHireDate().toString() : "–", alt));
                alt = !alt;
            }
            doc.add(table);
        }
    }

    // ── Revenue Report ───────────────────────────────────────────────────────

    public static void generateRevenueReport(File file, List<Object[]> monthlyRevenue) throws IOException {
        try (Document doc = createBaseDoc(file, "Financial Revenue Summary (12 Months)")) {
            
            // Calculating total revenue
            BigDecimal sum = BigDecimal.ZERO;
            for (Object[] r : monthlyRevenue) {
                sum = sum.add((BigDecimal) r[1]);
            }

            Table summary = new Table(2).useAllAvailableWidth();
            summary.setBackgroundColor(BG_LIGHT);
            summary.setMarginBottom(15);
            summary.addCell(new Cell().add(new Paragraph("Months Analyzed\n" + monthlyRevenue.size()).setBold().setTextAlignment(TextAlignment.CENTER)).setPadding(8).setBorder(null));
            summary.addCell(new Cell().add(new Paragraph("Cumulative Gross Revenue\n$" + String.format("%.2f", sum)).setBold().setTextAlignment(TextAlignment.CENTER)).setPadding(8).setBorder(null));
            doc.add(summary);

            Table table = createStyledTable(2);
            table.addHeaderCell(createHeaderCell("Month-Year"));
            table.addHeaderCell(createHeaderCell("Gross Revenue Amount"));

            boolean alt = false;
            for (Object[] r : monthlyRevenue) {
                table.addCell(createRowCell((String) r[0], alt));
                table.addCell(createRowCell("$" + String.format("%.2f", (BigDecimal) r[1]), alt));
                alt = !alt;
            }
            doc.add(table);
        }
    }

    // ── Invoice PDF Generation ───────────────────────────────────────────────

    public static void generateInvoice(File file, Sale sale) throws IOException {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A5); // A5 is standard receipt size
        Document doc = new Document(pdf);
        doc.setMargins(20, 20, 20, 20);

        // Header info
        doc.add(new Paragraph("OUTRIX ERP").setFontSize(14).setBold().setFontColor(PRIMARY_BLUE).setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Sales Invoice & Receipt").setFontSize(10).setTextAlignment(TextAlignment.CENTER).setFontColor(TEXT_DARK));
        doc.add(new Paragraph("==================================================").setFontSize(8).setFontColor(GRAY_BORDER));

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        infoTable.addCell(new Cell().add(new Paragraph("Invoice: " + sale.getInvoiceNumber()).setFontSize(9)).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(sale.getSaleDate())).setFontSize(9).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph("Customer: " + (sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in")).setFontSize(9)).setBorder(null));
        infoTable.addCell(new Cell().add(new Paragraph("Cashier: " + (sale.getUsername() != null ? sale.getUsername() : "")).setFontSize(9).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
        doc.add(infoTable);

        doc.add(new Paragraph("==================================================").setFontSize(8).setFontColor(GRAY_BORDER));

        // Line Items Table
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 15, 20, 25})).useAllAvailableWidth();
        table.addHeaderCell(new Cell().add(new Paragraph("Item").setBold().setFontSize(9)).setBorder(null).setBackgroundColor(BG_LIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold().setFontSize(9)).setBorder(null).setBackgroundColor(BG_LIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Price").setBold().setFontSize(9)).setBorder(null).setBackgroundColor(BG_LIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Subtotal").setBold().setFontSize(9).setTextAlignment(TextAlignment.RIGHT)).setBorder(null).setBackgroundColor(BG_LIGHT));

        for (SaleItem item : sale.getItems()) {
            table.addCell(new Cell().add(new Paragraph(item.getProductName()).setFontSize(8)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setFontSize(8)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", item.getUnitPrice())).setFontSize(8)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", item.getSubtotal())).setFontSize(8).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
        }
        doc.add(table);

        doc.add(new Paragraph("--------------------------------------------------").setFontSize(8).setFontColor(GRAY_BORDER));

        // Totals
        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
        totalsTable.addCell(new Cell().add(new Paragraph("Subtotal:").setFontSize(9)).setBorder(null));
        totalsTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", sale.getTotalAmount())).setFontSize(9).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
        totalsTable.addCell(new Cell().add(new Paragraph("Discount:").setFontSize(9)).setBorder(null));
        totalsTable.addCell(new Cell().add(new Paragraph("-$" + String.format("%.2f", sale.getDiscount())).setFontSize(9).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
        totalsTable.addCell(new Cell().add(new Paragraph("GRAND TOTAL:").setFontSize(10).setBold().setFontColor(PRIMARY_BLUE)).setBorder(null));
        totalsTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", sale.getGrandTotal())).setFontSize(10).setBold().setFontColor(PRIMARY_BLUE).setTextAlignment(TextAlignment.RIGHT)).setBorder(null));
        doc.add(totalsTable);

        doc.add(new Paragraph("==================================================").setFontSize(8).setFontColor(GRAY_BORDER));
        doc.add(new Paragraph("Thank you for your business!").setFontSize(8).setTextAlignment(TextAlignment.CENTER).setFontColor(TEXT_DARK));

        doc.close();
    }
}
