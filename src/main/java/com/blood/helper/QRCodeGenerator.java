package com.blood.helper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.context.annotation.Bean;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class QRCodeGenerator {
    public static String generateQRCode(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            return Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo mã QR: " + e.getMessage());
        }
    }

    public static String generateBarcodeImageBase64(String text, int width, int height) {
        try {
            Code128Writer barcodeWriter = new Code128Writer();

            BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.CODE_128, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi không thể tạo Barcode túi máu", e);
        }
    }
}
