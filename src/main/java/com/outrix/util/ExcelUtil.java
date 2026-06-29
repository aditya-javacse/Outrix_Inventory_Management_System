package com.outrix.util;

import com.outrix.dao.CategoryDAO;
import com.outrix.dao.SupplierDAO;
import com.outrix.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel Import/Export helper using Apache POI.
 */
public class ExcelUtil {

    private ExcelUtil() {}

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                if (val == (long) val) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    // ── Products ─────────────────────────────────────────────────────────────

    public static void exportProducts(List<Product> products, File file) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Products");
            String[] headers = {"Product Name", "Category", "Supplier", "Description", "Purchase Price", "Selling Price", "Quantity", "Low Stock Threshold", "Barcode"};
            
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(wb);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getProductName());
                row.createCell(1).setCellValue(p.getCategoryName());
                row.createCell(2).setCellValue(p.getSupplierName() != null ? p.getSupplierName() : "");
                row.createCell(3).setCellValue(p.getDescription() != null ? p.getDescription() : "");
                row.createCell(4).setCellValue(p.getPurchasePrice().doubleValue());
                row.createCell(5).setCellValue(p.getSellingPrice().doubleValue());
                row.createCell(6).setCellValue(p.getQuantity());
                row.createCell(7).setCellValue(p.getLowStockThreshold());
                row.createCell(8).setCellValue(p.getBarcode() != null ? p.getBarcode() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
    }

    public static List<Product> importProducts(File file) throws IOException, SQLException {
        List<Product> list = new ArrayList<>();
        CategoryDAO catDAO = new CategoryDAO();
        SupplierDAO supDAO = new SupplierDAO();

        List<Category> categories = catDAO.findAll();
        List<Supplier> suppliers = supDAO.findAll();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = getCellString(row.getCell(0));
                if (name.isEmpty()) continue; // skip invalid rows

                String catName  = getCellString(row.getCell(1));
                String supName  = getCellString(row.getCell(2));
                String desc     = getCellString(row.getCell(3));
                String ppStr    = getCellString(row.getCell(4));
                String spStr    = getCellString(row.getCell(5));
                String qtyStr   = getCellString(row.getCell(6));
                String thrStr   = getCellString(row.getCell(7));
                String barcode  = getCellString(row.getCell(8));

                // Find or create Category
                Category category = categories.stream().filter(c -> c.getName().equalsIgnoreCase(catName)).findFirst().orElse(null);
                if (category == null && !catName.isEmpty()) {
                    category = new Category();
                    category.setName(catName);
                    category.setDescription("Imported from Excel");
                    int newId = catDAO.insert(category);
                    category.setId(newId);
                    categories.add(category);
                }

                // Find Supplier
                Supplier supplier = suppliers.stream().filter(s -> s.getName().equalsIgnoreCase(supName)).findFirst().orElse(null);

                Product p = new Product();
                p.setProductName(name);
                p.setCategoryId(category != null ? category.getId() : 1); // default fallback
                p.setCategoryName(category != null ? category.getName() : "");
                p.setSupplierId(supplier != null ? supplier.getId() : null);
                p.setSupplierName(supplier != null ? supplier.getName() : null);
                p.setDescription(desc);
                p.setPurchasePrice(ppStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(ppStr));
                p.setSellingPrice(spStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(spStr));
                p.setQuantity(qtyStr.isEmpty() ? 0 : (int) Double.parseDouble(qtyStr));
                p.setLowStockThreshold(thrStr.isEmpty() ? 10 : (int) Double.parseDouble(thrStr));
                p.setBarcode(barcode.isEmpty() ? null : barcode);

                list.add(p);
            }
        }
        return list;
    }

    // ── Suppliers ────────────────────────────────────────────────────────────

    public static void exportSuppliers(List<Supplier> suppliers, File file) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Suppliers");
            String[] headers = {"Supplier Name", "Contact Number", "Email", "Address"};
            
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(wb);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Supplier s : suppliers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getName());
                row.createCell(1).setCellValue(s.getContactNumber() != null ? s.getContactNumber() : "");
                row.createCell(2).setCellValue(s.getEmail() != null ? s.getEmail() : "");
                row.createCell(3).setCellValue(s.getAddress() != null ? s.getAddress() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
    }

    public static List<Supplier> importSuppliers(File file) throws IOException {
        List<Supplier> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name  = getCellString(row.getCell(0));
                if (name.isEmpty()) continue;

                String phone = getCellString(row.getCell(1));
                String email = getCellString(row.getCell(2));
                String addr  = getCellString(row.getCell(3));

                Supplier s = new Supplier();
                s.setName(name);
                s.setContactNumber(phone);
                s.setEmail(email);
                s.setAddress(addr);
                list.add(s);
            }
        }
        return list;
    }

    // ── Customers ────────────────────────────────────────────────────────────

    public static void exportCustomers(List<Customer> customers, File file) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Customers");
            String[] headers = {"Customer Name", "Phone", "Email", "Address"};
            
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(wb);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Customer c : customers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getName());
                row.createCell(1).setCellValue(c.getPhone() != null ? c.getPhone() : "");
                row.createCell(2).setCellValue(c.getEmail() != null ? c.getEmail() : "");
                row.createCell(3).setCellValue(c.getAddress() != null ? c.getAddress() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
    }

    public static List<Customer> importCustomers(File file) throws IOException {
        List<Customer> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name  = getCellString(row.getCell(0));
                if (name.isEmpty()) continue;

                String phone = getCellString(row.getCell(1));
                String email = getCellString(row.getCell(2));
                String addr  = getCellString(row.getCell(3));

                Customer c = new Customer();
                c.setName(name);
                c.setPhone(phone);
                c.setEmail(email);
                c.setAddress(addr);
                list.add(c);
            }
        }
        return list;
    }

    // ── Sales ────────────────────────────────────────────────────────────────

    public static void exportSales(List<Sale> sales, File file) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sales History");
            String[] headers = {"Invoice Number", "Customer Name", "Cashier", "Subtotal", "Discount", "Grand Total", "Payment Method", "Date"};
            
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(wb);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Sale s : sales) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getInvoiceNumber());
                row.createCell(1).setCellValue(s.getCustomerName() != null ? s.getCustomerName() : "Walk-in");
                row.createCell(2).setCellValue(s.getUsername() != null ? s.getUsername() : "");
                row.createCell(3).setCellValue(s.getTotalAmount().doubleValue());
                row.createCell(4).setCellValue(s.getDiscount().doubleValue());
                row.createCell(5).setCellValue(s.getGrandTotal().doubleValue());
                row.createCell(6).setCellValue(s.getPaymentMethod());
                row.createCell(7).setCellValue(s.getSaleDate() != null ? s.getSaleDate().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
    }

    // ── Inventory Logs ───────────────────────────────────────────────────────

    public static void exportInventoryLogs(List<InventoryLog> logs, File file) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Stock Movement Log");
            String[] headers = {"Log ID", "Product Name", "Movement Type", "Quantity", "Previous Qty", "New Qty", "Reference", "Notes", "Date", "User"};
            
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(wb);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (InventoryLog l : logs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(l.getId());
                row.createCell(1).setCellValue(l.getProductName());
                row.createCell(2).setCellValue(l.getMovementType());
                row.createCell(3).setCellValue(l.getQuantity());
                row.createCell(4).setCellValue(l.getPreviousQty());
                row.createCell(5).setCellValue(l.getNewQty());
                row.createCell(6).setCellValue(l.getReference() != null ? l.getReference() : "");
                row.createCell(7).setCellValue(l.getNotes() != null ? l.getNotes() : "");
                row.createCell(8).setCellValue(l.getCreatedAt() != null ? l.getCreatedAt().toString() : "");
                row.createCell(9).setCellValue(l.getUsername() != null ? l.getUsername() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
    }
}
