package com.nt.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.nt.model.InvoiceData;

@Service
public class PdfGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(PdfGenerationService.class);
    private final String pdfStoragePath = "pdf_storage/"; // Ensure this directory exists

    public String generatePdf(InvoiceData invoiceData) throws Exception {
        // Ensure the PDF storage directory exists
        File directory = new File(pdfStoragePath);
        if (!directory.exists()) {
            logger.info("Creating directory: " + pdfStoragePath);
            Files.createDirectories(Paths.get(pdfStoragePath));
        }

        // Generate a unique file name based on invoice details
        String fileName = invoiceData.getSeller().replaceAll(" ", "_") + "_" + invoiceData.getBuyer().replaceAll(" ", "_") + ".pdf";
        File pdfFile = new File(pdfStoragePath + fileName);

        // Check if PDF already exists, return existing file path if so
        if (pdfFile.exists()) {
            logger.info("PDF already exists. Returning existing file: " + pdfFile.getAbsolutePath());
            return pdfFile.getAbsolutePath();
        }

        // Create a new PDF using iText
        try (FileOutputStream fos = new FileOutputStream(pdfFile); PdfWriter writer = new PdfWriter(fos)) {
            Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

            // Create a table for Seller and Buyer information
            Table sellerBuyerTable = new Table(2);
            sellerBuyerTable.setWidth(100); // Set table width to 100%

            // Seller Information
            Cell sellerCell = new Cell()
                    .add(new Paragraph("Seller: " + invoiceData.getSeller()).setBold())
                    .add(new Paragraph("GSTIN: " + invoiceData.getSellerGstin()))
                    .add(new Paragraph("Address: " + invoiceData.getSellerAddress()));
            sellerBuyerTable.addCell(sellerCell);

            // Buyer Information
            Cell buyerCell = new Cell()
                    .add(new Paragraph("Buyer: " + invoiceData.getBuyer()).setBold())
                    .add(new Paragraph("GSTIN: " + invoiceData.getBuyerGstin()))
                    .add(new Paragraph("Address: " + invoiceData.getBuyerAddress()));
            sellerBuyerTable.addCell(buyerCell);

            // Add the seller and buyer table to the document
            document.add(sellerBuyerTable);

            // Create a separate table for items
            Table itemsTable = new Table(new float[]{1, 1, 1, 1}); // Four columns: Item, Quantity, Rate, Amount
            itemsTable.setWidth(100); // Set table width to 100%

            // Add header for items
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Item")).setBold());
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Quantity")).setBold());
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Rate")).setBold());
            itemsTable.addHeaderCell(new Cell().add(new Paragraph("Amount")).setBold());

            // Add item information to the items table
            for (InvoiceData.Item item : invoiceData.getItems()) {
                itemsTable.addCell(new Cell().add(new Paragraph(item.getName())));
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity()))));
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getRate()))));
                itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getAmount()))));
            }

            // Add the items table to the document
            document.add(new Paragraph("\nItems:").setBold()); // Optional header for the items section
            document.add(itemsTable);

            // Close the document
            document.close();
            logger.info("PDF generated successfully at: " + pdfFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Error creating PDF", e);
            throw new Exception("Error creating PDF", e);
        }

        return pdfFile.getAbsolutePath();
    }

    public String getPdfPath(String seller, String buyer) {
        String fileName = seller.replaceAll(" ", "_") + "_" + buyer.replaceAll(" ", "_") + ".pdf";
        return pdfStoragePath + fileName;
    }
}
// download url: http://localhost:9999/api/pdf/download/XYZ%20Pvt.%20Ltd./Vedant%20Computers