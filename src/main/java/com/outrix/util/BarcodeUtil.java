package com.outrix.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for generating barcodes and QR codes using ZXing.
 */
public class BarcodeUtil {

    /**
     * Generates a Code128 barcode image.
     *
     * @param text   the content to encode
     * @param width  image width in pixels
     * @param height image height in pixels
     * @return BufferedImage of the barcode
     */
    public BufferedImage generateBarcode(String text, int width, int height) throws WriterException {
        Code128Writer writer = new Code128Writer();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.CODE_128, width, height);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    /**
     * Generates a QR code image.
     *
     * @param text   the content to encode
     * @param size   image size (width = height) in pixels
     * @return BufferedImage of the QR code
     */
    public BufferedImage generateQRCode(String text, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    /**
     * Generates a unique product barcode string in format PRD-YYYYMMDD-{id}.
     */
    public static String generateProductBarcode(int productId) {
        String date = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        return String.format("PRD-%s-%04d", date, productId);
    }
}
