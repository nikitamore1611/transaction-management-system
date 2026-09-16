package com.transaction.management.service;

import com.transaction.management.dto.MonthlyReportResponse;
import com.transaction.management.entity.Transaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class MonthlyReportPdfService {

    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    private static final float LEFT_MARGIN = 40;
    private static final float TOP_MARGIN = 50;
    private static final float BOTTOM_MARGIN = 50;

    private static final float ROW_HEIGHT = 18;

    public byte[] generatePdf(MonthlyReportResponse report)
            throws IOException {

        try (PDDocument document = new PDDocument()) {

            PDType1Font boldFont =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA_BOLD
                    );

            PDType1Font normalFont =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    );

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream =
                    new PDPageContentStream(document, page);

            float y = PAGE_HEIGHT - TOP_MARGIN;

            // =========================
            // TITLE
            // =========================

            contentStream.beginText();
            contentStream.setFont(boldFont, 18);
            contentStream.newLineAtOffset(160, y);
            contentStream.showText("Monthly Transaction Report");
            contentStream.endText();

            y -= 40;

            // =========================
            // REPORT PERIOD
            // =========================

            y = writeLine(
                    contentStream,
                    boldFont,
                    "Report Period: "
                            + report.getMonth()
                            + "/"
                            + report.getYear(),
                    LEFT_MARGIN,
                    y,
                    12
            );

            y -= 10;

            // =========================
            // SUMMARY
            // =========================

            y = writeLine(
                    contentStream,
                    normalFont,
                    "Opening Balance: "
                            + formatAmount(report.getOpeningBalance()),
                    LEFT_MARGIN,
                    y,
                    11
            );

            y = writeLine(
                    contentStream,
                    normalFont,
                    "Total Credit: "
                            + formatAmount(report.getTotalCredit()),
                    LEFT_MARGIN,
                    y,
                    11
            );

            y = writeLine(
                    contentStream,
                    normalFont,
                    "Total Debit: "
                            + formatAmount(report.getTotalDebit()),
                    LEFT_MARGIN,
                    y,
                    11
            );

            y = writeLine(
                    contentStream,
                    normalFont,
                    "Closing Balance: "
                            + formatAmount(report.getClosingBalance()),
                    LEFT_MARGIN,
                    y,
                    11
            );

            y = writeLine(
                    contentStream,
                    normalFont,
                    "Total Transactions: "
                            + report.getTotalTransactions(),
                    LEFT_MARGIN,
                    y,
                    11
            );

            y -= 10;

            // =========================
            // MESSAGE
            // =========================

            y = writeLine(
                    contentStream,
                    normalFont,
                    report.getMessage(),
                    LEFT_MARGIN,
                    y,
                    11
            );

            y -= 15;

            // =========================
            // TRANSACTION HEADING
            // =========================

            y = writeLine(
                    contentStream,
                    boldFont,
                    "Transaction Details",
                    LEFT_MARGIN,
                    y,
                    12
            );

            y -= 15;

            // =========================
            // TABLE HEADER
            // =========================

            y = writeTableHeader(
                    contentStream,
                    boldFont,
                    y
            );

            DateTimeFormatter dateFormatter =
                    DateTimeFormatter.ofPattern("dd-MM-yyyy");

            // =========================
            // TRANSACTIONS
            // =========================

            for (Transaction transaction :
                    report.getTransactions()) {

                // Create a new page when required
                if (y < BOTTOM_MARGIN + ROW_HEIGHT) {

                    contentStream.close();

                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);

                    contentStream =
                            new PDPageContentStream(
                                    document,
                                    page
                            );

                    y = PAGE_HEIGHT - TOP_MARGIN;

                    // Continue table header on new page
                    y = writeTableHeader(
                            contentStream,
                            boldFont,
                            y
                    );
                }

                String date =
                        transaction.getTransactionDate()
                                .format(dateFormatter);

                String type =
                        transaction.getType()
                                .toString();

                String amount =
                        formatAmount(
                                transaction.getAmount()
                        );

                String reason =
                        transaction.getReason();

                if (reason == null) {
                    reason = "";
                }

                if (reason.length() > 28) {
                    reason = reason.substring(0, 28);
                }

                String balance =
                        formatAmount(
                                transaction.getBalance()
                        );

                // Date
                writeCell(
                        contentStream,
                        normalFont,
                        date,
                        40,
                        y,
                        9
                );

                // Type
                writeCell(
                        contentStream,
                        normalFont,
                        type,
                        115,
                        y,
                        9
                );

                // Amount
                writeCell(
                        contentStream,
                        normalFont,
                        amount,
                        170,
                        y,
                        9
                );

                // Reason
                writeCell(
                        contentStream,
                        normalFont,
                        reason,
                        245,
                        y,
                        9
                );

                // Balance
                writeCell(
                        contentStream,
                        normalFont,
                        balance,
                        475,
                        y,
                        9
                );

                y -= ROW_HEIGHT;
            }

            contentStream.close();

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            document.save(outputStream);

            return outputStream.toByteArray();
        }
    }

    // =========================
    // TABLE HEADER
    // =========================

    private float writeTableHeader(
            PDPageContentStream contentStream,
            PDType1Font boldFont,
            float y
    ) throws IOException {

        writeCell(
                contentStream,
                boldFont,
                "Date",
                40,
                y,
                10
        );

        writeCell(
                contentStream,
                boldFont,
                "Type",
                115,
                y,
                10
        );

        writeCell(
                contentStream,
                boldFont,
                "Amount",
                170,
                y,
                10
        );

        writeCell(
                contentStream,
                boldFont,
                "Reason",
                245,
                y,
                10
        );

        writeCell(
                contentStream,
                boldFont,
                "Balance",
                475,
                y,
                10
        );

        return y - ROW_HEIGHT;
    }

    // =========================
    // WRITE NORMAL LINE
    // =========================

    private float writeLine(
            PDPageContentStream contentStream,
            PDType1Font font,
            String text,
            float x,
            float y,
            float fontSize
    ) throws IOException {

        contentStream.beginText();

        contentStream.setFont(font, fontSize);

        contentStream.newLineAtOffset(x, y);

        contentStream.showText(text);

        contentStream.endText();

        return y - 20;
    }

    // =========================
    // WRITE TABLE CELL
    // =========================

    private void writeCell(
            PDPageContentStream contentStream,
            PDType1Font font,
            String text,
            float x,
            float y,
            float fontSize
    ) throws IOException {

        contentStream.beginText();

        contentStream.setFont(font, fontSize);

        contentStream.newLineAtOffset(x, y);

        contentStream.showText(text);

        contentStream.endText();
    }

    // =========================
    // FORMAT AMOUNT
    // =========================

    private String formatAmount(BigDecimal amount) {

        if (amount == null) {
            return "INR 0.00";
        }

        return "INR "
                + amount.setScale(2).toString();
    }
}