package de.teamA.SWT.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.teamA.SWT.entities.Borrowing;
import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.Wish;
import de.teamA.SWT.repository.MediumRepository;

@Service
public class PdfService {

    Logger logger = LoggerFactory.getLogger(PdfService.class);

    private final int MARGIN = 25;
    private final float SPACING_PARAGRAPHS = 16.0f;
    private final float SPACING_SIGNATURE = 56.f;
    private final int FONTSIZE_DEFAULT = 12;
    private final int FONTSIZE_HEADER = 14;
    private final int FONTSIZE_DESCRIPTION = 8;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final String borrowHeadingFile = "templates/receipts/student_borrowing/heading.txt";
    private final String borrowFormFieldsFile = "templates/receipts/student_borrowing/form_fields.txt";
    private final String borrowLegalNotesFile = "templates/receipts/student_borrowing/legal_notes.txt";
    private final String borrowSignaturesFile = "templates/receipts/student_borrowing/signatures.txt";
    private final String returnHeadingFile = "templates/receipts/student_return/heading.txt";
    private final String returnFormFieldsFile = "templates/receipts/student_return/form_fields.txt";
    private final String returnLegalNotesFile = "templates/receipts/student_return/legal_notes.txt";
    private final String returnSignaturesFile = "templates/receipts/student_return/signatures.txt";
    private MediumRepository mediumRepository;
    private String borrowHeading;
    private String borrowLegalNotes;
    private List<String> borrowFormFields;
    private List<String> borrowSignatures;
    private String returnHeading;
    private String returnLegalNotes;
    private List<String> returnFormFields;
    private List<String> returnSignatures;

    public PdfService(MediumRepository mediumRepository) {
        this.mediumRepository = mediumRepository;

        // Borrowing Receipt
        borrowHeading = loadString(borrowHeadingFile);
        borrowLegalNotes = loadStringFromMultilineFile(borrowLegalNotesFile);
        borrowFormFields = loadStringList(borrowFormFieldsFile);
        borrowSignatures = loadStringList(borrowSignaturesFile);

        // Return Receipt
        returnHeading = loadString(returnHeadingFile);
        returnLegalNotes = loadStringFromMultilineFile(returnLegalNotesFile);
        returnFormFields = loadStringList(returnFormFieldsFile);
        returnSignatures = loadStringList(returnSignaturesFile);
    }

    public byte[] generateStudentReceipt(ReceiptType type, Borrowing borrowing, User currentActor) {
        Document document = new Document(PageSize.A5, MARGIN, MARGIN, MARGIN, MARGIN);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            PdfWriter.getInstance(document, out);

            document.open();

            if (type.equals(ReceiptType.BORROW_RECEIPT)) {
                buildBorrowReceipt(document, borrowing, currentActor);
            }
            if (type.equals(ReceiptType.RETURN_RECEIPT)) {

                buildReturnReceipt(document, borrowing, currentActor);
            }

            document.close();

        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }

        return out.toByteArray();

    }

    private void buildReturnReceipt(Document document, Borrowing borrowing, User currentActor)
            throws DocumentException {

        // Font setup
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, FONTSIZE_HEADER, BaseColor.BLACK);
        Font defaultFont = FontFactory.getFont(FontFactory.HELVETICA, FONTSIZE_DEFAULT, BaseColor.BLACK);
        Font descriptionFont = FontFactory.getFont(FontFactory.HELVETICA, FONTSIZE_DESCRIPTION, BaseColor.BLACK);

        // Heading
        document.addTitle(returnHeading);
        Paragraph headingParagraph = new Paragraph(returnHeading, headingFont);

        headingParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(headingParagraph);
        document.add(Chunk.NEWLINE);

        // Load Strings from borrowing (make sure they're in an order matching the
        // form_fields list)
        List<String> dataStrings = new ArrayList<>();
        Optional<Medium> optionalMedium = mediumRepository.findById(borrowing.getPhysical().getMediumId());
        if (!optionalMedium.isPresent()) {
            throw new RuntimeException("Medium must be present!");
        }
        Medium medium = optionalMedium.get();
        String title = medium.getBooktitle();
        if (medium.getTitle() != null && !medium.getTitle().isEmpty()) {
            title = title + ": " + medium.getTitle();
        }
        dataStrings.add(title);
        dataStrings.add(borrowing.getPhysical().getId().toString());

        dataStrings.add(borrowing.getPhysical().getOwner().getName());

        if (borrowing.getBorrowDate() != null) {
            dataStrings.add(dateFormatter.format(borrowing.getBorrowDate()));
        } else {
            dataStrings.add("");
        }

        if (borrowing.getResponsibleStaff() != null) {
            dataStrings.add(borrowing.getResponsibleStaff().getName());
        } else {
            dataStrings.add("");
        }

        // Build data fields
        for (int i = 0; i < dataStrings.size(); i++) {

            String data = dataStrings.get(i);
            String description = returnFormFields.get(i);

            Paragraph formFieldParagraph = new Paragraph();
            formFieldParagraph.setAlignment(Paragraph.ALIGN_LEFT);
            formFieldParagraph.setSpacingAfter(SPACING_PARAGRAPHS);

            Chunk dataChunk = new Chunk(data, defaultFont);
            Chunk descriptionChunk = new Chunk(description, descriptionFont);

            formFieldParagraph.add(dataChunk);
            formFieldParagraph.add(Chunk.NEWLINE);
            formFieldParagraph.add(descriptionChunk);
            document.add(formFieldParagraph);

        }

        // legal notes
        Paragraph legalNotesParagraph = new Paragraph(returnLegalNotes, defaultFont);
        document.add(legalNotesParagraph);

        // create Signature
        Paragraph signatureParagraph = new Paragraph();
        signatureParagraph.setSpacingBefore(SPACING_SIGNATURE);
        Chunk spacer = new Chunk(new VerticalPositionMark());

        // Staff
        String signatureStaffName = currentActor.getName();
        String signatureStaff = returnSignatures.get(0);

        Chunk signatureStaffNameChunk = new Chunk(signatureStaffName, descriptionFont);
        Chunk signatureStaffChunk = new Chunk(signatureStaff, descriptionFont);

        // Student
        String signatureStudentName = borrowing.getBorrower().getName();
        String signatureStudent = returnSignatures.get(1);

        Chunk signatureStudentNameChunk = new Chunk(signatureStudentName, descriptionFont);
        Chunk signatureStudentChunk = new Chunk(signatureStudent, descriptionFont);

        signatureParagraph.add(signatureStaffNameChunk);
        signatureParagraph.add(spacer);
        signatureParagraph.add(signatureStudentNameChunk);
        signatureParagraph.add(Chunk.NEWLINE);
        signatureParagraph.add(signatureStaffChunk);
        signatureParagraph.add(spacer);
        signatureParagraph.add(signatureStudentChunk);

        document.add(signatureParagraph);

        // Location + Date
        String location = returnSignatures.get(2);
        String date = LocalDate.now().format(dateFormatter);

        Paragraph locDateParagraph = new Paragraph();
        locDateParagraph.setAlignment(Element.ALIGN_RIGHT);

        locDateParagraph.add(location);
        locDateParagraph.add(" ");
        locDateParagraph.add(date);

        document.add(locDateParagraph);

    }

    private void buildBorrowReceipt(Document document, Borrowing borrowing, User currentActor)
            throws DocumentException {

        // Font setup
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, FONTSIZE_HEADER, BaseColor.BLACK);
        Font defaultFont = FontFactory.getFont(FontFactory.HELVETICA, FONTSIZE_DEFAULT, BaseColor.BLACK);
        Font descriptionFont = FontFactory.getFont(FontFactory.HELVETICA, FONTSIZE_DESCRIPTION, BaseColor.BLACK);

        // Heading
        document.addTitle(borrowHeading);
        Paragraph headingParagraph = new Paragraph(borrowHeading, headingFont);
        headingParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(headingParagraph);
        document.add(Chunk.NEWLINE);

        // Load Strings from borrowing (make sure they're in an order matching the
        // form_fields list)
        List<String> dataStrings = new ArrayList<>();
        Optional<Medium> optionalMedium = mediumRepository.findById(borrowing.getPhysical().getMediumId());
        if (!optionalMedium.isPresent()) {
            throw new RuntimeException("Medium must be present!");
        }
        Medium medium = optionalMedium.get();
        String title = medium.getBooktitle();
        if (medium.getTitle() != null && !medium.getTitle().isEmpty()) {
            title = title + ": " + medium.getTitle();
        }
        dataStrings.add(title);
        dataStrings.add(borrowing.getPhysical().getId().toString());

        dataStrings.add(borrowing.getBorrower().getName());
        dataStrings.add(borrowing.getBorrower().getEmail());
        if (borrowing.getDueDate() != null) {
            dataStrings.add(dateFormatter.format(borrowing.getDueDate()));
        } else {
            dataStrings.add("");
        }
        dataStrings.add(borrowing.getPhysical().getOwner().getName());

        // Build data fields
        for (int i = 0; i < dataStrings.size(); i++) {

            String data = dataStrings.get(i);
            String description = borrowFormFields.get(i);

            Paragraph formFieldParagraph = new Paragraph();
            formFieldParagraph.setAlignment(Paragraph.ALIGN_LEFT);
            formFieldParagraph.setSpacingAfter(SPACING_PARAGRAPHS);

            Chunk dataChunk = new Chunk(data, defaultFont);
            Chunk descriptionChunk = new Chunk(description, descriptionFont);

            formFieldParagraph.add(dataChunk);
            formFieldParagraph.add(Chunk.NEWLINE);
            formFieldParagraph.add(descriptionChunk);
            document.add(formFieldParagraph);
        }

        // legal notes
        Paragraph legalNotesParagraph = new Paragraph(borrowLegalNotes, defaultFont);
        document.add(legalNotesParagraph);

        // create Signature
        Paragraph signatureParagraph = new Paragraph();
        signatureParagraph.setSpacingBefore(SPACING_SIGNATURE);
        Chunk spacer = new Chunk(new VerticalPositionMark());

        // Staff
        String signatureStaffName = currentActor.getName();
        String signatureStaff = borrowSignatures.get(0);

        Chunk signatureStaffNameChunk = new Chunk(signatureStaffName, descriptionFont);
        Chunk signatureStaffChunk = new Chunk(signatureStaff, descriptionFont);

        // Student
        String signatureStudentName = borrowing.getBorrower().getName();
        String signatureStudent = borrowSignatures.get(1);

        Chunk signatureStudentNameChunk = new Chunk(signatureStudentName, descriptionFont);
        Chunk signatureStudentChunk = new Chunk(signatureStudent, descriptionFont);

        signatureParagraph.add(signatureStaffNameChunk);
        signatureParagraph.add(spacer);
        signatureParagraph.add(signatureStudentNameChunk);
        signatureParagraph.add(Chunk.NEWLINE);
        signatureParagraph.add(signatureStaffChunk);
        signatureParagraph.add(spacer);
        signatureParagraph.add(signatureStudentChunk);

        document.add(signatureParagraph);

        // Location + Date
        String location = returnSignatures.get(2);
        String date = LocalDate.now().format(dateFormatter);

        Paragraph locDateParagraph = new Paragraph();
        locDateParagraph.setAlignment(Element.ALIGN_RIGHT);

        locDateParagraph.add(location);
        locDateParagraph.add(" ");
        locDateParagraph.add(date);

        document.add(locDateParagraph);

    }

    private String loadString(String path) {

        Path legalNotesPath = Paths.get("./", path);

        List<String> stringList;

        try {
            stringList = Files.readAllLines(legalNotesPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

        return stringList.get(0);
    }

    private String loadStringFromMultilineFile(String path) {

        Path legalNotesPath = Paths.get("./", path);
        List<String> stringList;

        try {
            stringList = Files.readAllLines(legalNotesPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return String.join(" ", stringList);
    }

    private List<String> loadStringList(String path) {

        Path legalNotesPath = Paths.get("./", path);

        List<String> stringList;

        try {
            stringList = Files.readAllLines(legalNotesPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stringList;

    }

    public enum ReceiptType {
        BORROW_RECEIPT, RETURN_RECEIPT
    }

    // Gernerates Wish Pdf with alls selceted Wishes and shows Title, ISBN and
    // Priority as a List
    public byte[] generateWishPdf(List<Wish> selectedWishes) {
        Document document = new Document(PageSize.A5, MARGIN, MARGIN, MARGIN, MARGIN);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            PdfWriter.getInstance(document, out);

            document.open();
            document.addTitle("Wishlist");
            document.add(new Paragraph("Folgende Bücher werden sich gewünscht: "));
            document.add(new Paragraph(" "));

            for (Wish wish : selectedWishes) {
                Paragraph wishli = new Paragraph();
                String isbn = wish.getIsbn();
                String priotrity = wish.getPriority();
                wishli.add("- " + wish.getTitle() + ". (Isbn: " + isbn + "  Priority: " + priotrity + ")");
                document.add(wishli);
            }

            document.close();

        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }

        return out.toByteArray();

    }

}
