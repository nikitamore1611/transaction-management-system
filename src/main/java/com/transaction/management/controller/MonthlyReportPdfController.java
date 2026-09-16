package com.transaction.management.controller;

import com.transaction.management.dto.MonthlyReportResponse;
import com.transaction.management.entity.User;
import com.transaction.management.repository.UserRepository;
import com.transaction.management.service.MonthlyReportPdfService;
import com.transaction.management.service.MonthlyReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class MonthlyReportPdfController {

    private final MonthlyReportService monthlyReportService;
    private final MonthlyReportPdfService monthlyReportPdfService;
    private final UserRepository userRepository;

    public MonthlyReportPdfController(
            MonthlyReportService monthlyReportService,
            MonthlyReportPdfService monthlyReportPdfService,
            UserRepository userRepository) {

        this.monthlyReportService = monthlyReportService;
        this.monthlyReportPdfService = monthlyReportPdfService;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser(
            Authentication authentication) {

        return userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(() ->
                new RuntimeException("User not found"));
    }

    @GetMapping("/monthly/pdf")
    public ResponseEntity<byte[]> downloadMonthlyReportPdf(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {

        try {

            User user = getLoggedInUser(authentication);

            MonthlyReportResponse report =
                    monthlyReportService.generateMonthlyReport(
                            user.getId(),
                            year,
                            month
                    );

            byte[] pdf =
                    monthlyReportPdfService.generatePdf(report);

            String fileName =
                    "Monthly-Transaction-Report-"
                            + year
                            + "-"
                            + String.format("%02d", month)
                            + ".pdf";

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\""
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdf.length)
                    .body(pdf);

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Unable to generate monthly PDF report: "
                            + exception.getMessage()
            );
        }
    }
}