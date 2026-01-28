package QuarryDemo.util;

import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.sql.*;
import java.util.*;

public class NumbrimargidExporter {

    public static void eksportiNumbrimargidExcel(Connection connection, String failinimi) {
        String home = System.getProperty("user.home");
        String desktopPath = home + "/Desktop";
        String filePath = desktopPath + "/"+failinimi;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Numbrimärgid");

            // Numbrimärkide saamine db-st
            Map<String, List<String>> firmaToNumbrid = new LinkedHashMap<>();
            List<String> firmad = new ArrayList<>();

            String sql = "SELECT firma, numbrimark FROM numbrimargid ORDER BY firma";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String firma = rs.getString("firma");
                    String numbrimark = rs.getString("numbrimark");

                    if (!firmaToNumbrid.containsKey(firma)) {
                        firmad.add(firma);
                        firmaToNumbrid.put(firma, new ArrayList<>());
                    }
                    firmaToNumbrid.get(firma).add(numbrimark);
                }
            }

            firmad.sort((f1, f2) -> Integer.compare(
                    firmaToNumbrid.get(f2).size(),
                    firmaToNumbrid.get(f1).size()
            ));
            XSSFColor päis = new XSSFColor(java.awt.Color.decode("#FFB74D"), null);
            XSSFColor värv1 = new XSSFColor(java.awt.Color.decode("#FFF3E0"), null);
            XSSFColor värv2 = new XSSFColor(java.awt.Color.decode("#FFE0B2"), null);

            // Stiil
            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setColor(IndexedColors.WHITE.getIndex());
            boldFont.setFontHeightInPoints((short) 10);
            headerStyle.setFont(boldFont);
            headerStyle.setFillForegroundColor(päis);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            border(headerStyle);

            Font regularfont= workbook.createFont();
            regularfont.setFontHeightInPoints((short) 10);

            CellStyle colorStyle = workbook.createCellStyle();
            colorStyle.setFillForegroundColor(värv2);
            colorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            colorStyle.setFont(regularfont);
            border(colorStyle);

            CellStyle whiteStyle = workbook.createCellStyle();
            whiteStyle.setFillForegroundColor(värv1);
            whiteStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            whiteStyle.setFont(regularfont);
            border(whiteStyle);

            int maxCols = firmaToNumbrid.values().stream().mapToInt(List::size).max().orElse(0);

            //andmed

            for (int r = 0; r < firmad.size(); r++) {
                String firma = firmad.get(r);
                List<String> numbrid = firmaToNumbrid.get(firma);

                Row row = sheet.createRow(r);

                // Firma nimi veergu 0
                Cell cellFirma = row.createCell(0);
                cellFirma.setCellValue(firma);
                cellFirma.setCellStyle(headerStyle);

                //nrmärgid
                CellStyle rowStyle = (r % 2 == 0) ? colorStyle : whiteStyle;
                for (int c = 0; c < maxCols; c++) {
                    Cell cell = row.createCell(c + 1);
                    if (c < numbrid.size() && numbrid.get(c) != null) {
                        cell.setCellValue(numbrid.get(c));
                    }
                    cell.setCellStyle(rowStyle);
                }
            }

            // suurus
            for (int i = 0; i <= maxCols; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
                Alert alert=new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Numbrimärgid");
                alert.setHeaderText("Edukalt salvestatud");
                alert.setContentText(filePath);
                alert.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void border(CellStyle headerStyle) {
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        headerStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.index);
        headerStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.index);
        headerStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.index);
        headerStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.index);
    }
}

