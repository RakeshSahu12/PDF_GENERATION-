package com.nt.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nt.model.InvoiceData;
import com.nt.service.PdfGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private static final Logger logger = LoggerFactory.getLogger(PdfController.class);

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generatePdf(@RequestBody InvoiceData invoiceData) {
        try {
            // Call service to generate the PDF
            String filePath = pdfGenerationService.generatePdf(invoiceData);
            return ResponseEntity.ok("PDF generated successfully at: " + filePath);
        } catch (Exception e) {
            logger.error("Error generating PDF: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating PDF");
        }
    }

    @GetMapping(value = "/download/{seller}/{buyer}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable String seller,
            @PathVariable String buyer) {
        try {
            // Call service to get the PDF path
            String filePath = pdfGenerationService.getPdfPath(seller, buyer);
            File file = new File(filePath);

            if (!file.exists()) {
                logger.error("File does not exist at path: " + filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Prepare HTTP headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", file.getName());

            // Return the file as byte array
            Path pdfPath = Paths.get(file.getAbsolutePath());
            byte[] fileData = Files.readAllBytes(pdfPath);

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error downloading PDF: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
