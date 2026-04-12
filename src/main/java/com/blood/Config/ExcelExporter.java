package com.blood.Config;

import com.blood.Model.BloodBag;
import com.blood.Model.Donor;
import com.blood.Model.EventRegistration;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    public static byte[] exportIssueInvoiceToExcel(String hospitalName, String exportDate, List<BloodBag> exportedBags) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Hóa Đơn Xuất Kho");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Row rowTitle = sheet.createRow(0);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("PHIẾU XUẤT CHẾ PHẨM MÁU");
            cellTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            Row rowHospital = sheet.createRow(2);
            rowHospital.createCell(0).setCellValue("Đơn vị nhận:");
            rowHospital.createCell(1).setCellValue(hospitalName);
            rowHospital.getCell(0).setCellStyle(boldStyle);

            Row rowDate = sheet.createRow(3);
            rowDate.createCell(0).setCellValue("Ngày xuất:");
            rowDate.createCell(1).setCellValue(exportDate);
            rowDate.getCell(0).setCellStyle(boldStyle);

            Row headerRow = sheet.createRow(5);
            String[] columns = {"STT", "Mã Túi (BagCode)", "Nhóm Máu", "Chế Phẩm", "Thể Tích (ml)"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(boldStyle);
            }

            int rowIdx = 6;
            for (int i = 0; i < exportedBags.size(); i++) {
                BloodBag bag = exportedBags.get(i);
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(bag.getBagCode());
                row.createCell(2).setCellValue(bag.getBloodType() + bag.getRhFactor());
                row.createCell(3).setCellValue(bag.getProductType().name());
                row.createCell(4).setCellValue(bag.getVolume());
            }

            Row rowTotal = sheet.createRow(rowIdx + 1);
            rowTotal.createCell(3).setCellValue("TỔNG CỘNG:");
            rowTotal.getCell(3).setCellStyle(boldStyle);
            rowTotal.createCell(4).setCellValue(exportedBags.size() + " túi");
            rowTotal.getCell(4).setCellStyle(boldStyle);

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xuất hóa đơn", e);
        }
    }

     public static byte[] exportDonorListToExcel(List<EventRegistration> registrationList) {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                Sheet sheet = workbook.createSheet("Lịch Sử Hiến Máu");

                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFont(headerFont);

                Row headerRow = sheet.createRow(0);
                String[] columns = {"STT", "Họ Tên", "CCCD/CMND", "Nhóm Máu", "Ngày Hiến", "Thể Tích (ml)"};

                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                int rowIdx = 1;
                for (EventRegistration reg : registrationList) {
                    Row row = sheet.createRow(rowIdx++);

                    row.createCell(0).setCellValue(rowIdx - 1);

                    String fullName = "";
                    String cccd = "";
                    String bloodGroup = "";
                    String collectedDate = "Chưa lấy máu";
                    Integer volume = 0;

                    if (reg.getDonor() != null) {
                        fullName = reg.getDonor().getFullName() != null ? reg.getDonor().getFullName() : "";
                        cccd = reg.getDonor().getCccd() != null ? reg.getDonor().getCccd() : "";

                        String type = reg.getDonor().getBloodType() != null ? reg.getDonor().getBloodType().toString() : "";
                        String rh = reg.getDonor().getRhFactor() != null ? reg.getDonor().getRhFactor() : "";
                        bloodGroup = type + rh;
                    }

                    if (reg.getActualVolume() != null) {
                        volume = reg.getActualVolume();
                    }

                    if (reg.getBloodBag() != null && !reg.getBloodBag().isEmpty()) {

                        BloodBag firstBag = reg.getBloodBag().get(0);

                        if (firstBag.getCollectedAt() != null) {
                            collectedDate = firstBag.getCollectedAt().format(dateFormatter);
                        }
                    }

                    row.createCell(1).setCellValue(fullName);
                    row.createCell(2).setCellValue(cccd);
                    row.createCell(3).setCellValue(bloodGroup);
                    row.createCell(4).setCellValue(collectedDate);
                    row.createCell(5).setCellValue(volume);
                }

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(out);
                return out.toByteArray();

            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi xuất file Excel Danh sách người hiến", e);
            }
    }
}
