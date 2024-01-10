package de.teamA.SWT.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.ValidationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.teamA.SWT.entities.Borrowing;
import de.teamA.SWT.entities.BorrowingStatus;
import de.teamA.SWT.entities.BorrowingWithMedium;
import de.teamA.SWT.entities.PhysicalMedium;
import de.teamA.SWT.entities.PhysicalWithBorrowings;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.service.AuthService;
import de.teamA.SWT.service.BorrowingService;
import de.teamA.SWT.service.LibraryService;
import de.teamA.SWT.service.PdfService;
import de.teamA.SWT.service.PhysicalService;
import de.teamA.SWT.service.UserService;

@RestController
@RequestMapping("api/borrow")
@CrossOrigin
public class BorrowingController {

    @Autowired
    LibraryService libraryService;

    @Autowired
    PdfService pdfService;

    @Autowired
    UserService userService;

    @Autowired
    BorrowingService borrowingService;

    @Autowired
    PhysicalService physicalService;

    @Autowired
    AuthService authService;

    /**
     * If the selected physical isnt' currently borrowed, the book will be borrowed.
     * If the invoking user is a student, the borrowing will be requested.
     *
     * @param date     Date when the book shall be returned
     * @param mediumId id of the PhysicalMedium, which shall be borrowed
     * @return JsonResponse which says if the borrowing(-request) was successfully
     *         or why it wasn't successfully.
     */
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
    public JsonResponse borrow(@RequestParam(value = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "medium") long mediumId) {
        try {
            if (!libraryService.getAvailability(mediumId).equals("BORROWED")) {

                if (authService.isStudent()) {
                    return borrowingService.borrowStudent(mediumId);
                } else {
                    return borrowingService.borrow(date, mediumId);
                }
            }
            return new JsonResponse(500, "This medium is currently not available");
        } catch (EntityNotFoundException | ValidationException e) {
            return new JsonResponse(500, e.getMessage());
        }
    }

    /**
     * Possibility for staff to borrow books for students, if for example the
     * requested book isn't available anymore or the staff suggest another medium.
     *
     * @param date     Date when the book shall be returned
     * @param userId   Id of the student, for whom the book shall be borrowed
     * @param mediumId id of the PhysicalMedium, which shall be borrowed
     * @return JsonResponse which says if the borrowing-request was successfully or
     *         why it wasn't successfully.
     */
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.POST)
    public JsonResponse borrow(@RequestParam(value = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(value = "student") String userId, @RequestParam(value = "medium") long mediumId) {
        try {
            return borrowingService.borrowForStudent(date, userId, mediumId);
        } catch (ValidationException e) {
            return new JsonResponse(500, e.getMessage());
        }
    }

    /**
     * Updates the status of a physical book.
     * 
     * @param physical the physical with its new status
     * @return JsonResponse which says if the update was successful.
     */
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN')")
    @PostMapping(value = "/physical")
    public JsonResponse updatePhysicalStatus(@RequestBody PhysicalMedium physical) {
        try {
            return physicalService.updatePhysicalStatus(physical);
        } catch (ValidationException e) {
            return new JsonResponse(500, e.getMessage());
        }
    }

    /**
     * Method to accept the borrowing request of student. While accepting the return
     * date will be setted
     *
     * @param borrowingId Id of the to accepting borrowing
     * @param date        return Date for the medium
     * @return JsonResponse which says if the borrowing was successfully accepted or
     *         why it wasn't.
     */
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/accept")
    public JsonResponse acceptBorrowing(@RequestParam(value = "borrowing") long borrowingId,
            @RequestParam(value = "dueDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return borrowingService.acceptBorrowing(borrowingId, date);
    }

    /**
     * Method to decline the borrowing request of student.
     *
     * @param borrowingId Id of the to declining borrowing
     * @return JsonResponse which says if the borrowing was successfully declined or
     *         why it wasn't.
     */
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/decline")
    public JsonResponse declineBorrowing(@RequestParam(value = "borrowing") long borrowingId) {
        return borrowingService.declineBorrowing(borrowingId);
    }

    /**
     * Method to extend a borrowing
     *
     * @param borrowingId Id of the to extending borrowing
     * @param date        new return date
     * @return JsonResponse which says if the borrowing was successfully extended or
     *         why it wasn't.
     */
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/extend")
    public JsonResponse extendBorrowing(@RequestParam(value = "borrowing") long borrowingId,
            @RequestParam(value = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            return borrowingService.extendBorrowing(borrowingId, date);
        } catch (ValidationException e) {
            return new JsonResponse(500, e.getMessage());
        }
    }

    /**
     * Method to return a medium
     *
     * @param borrowingId Id of the to returning borrowing
     * @return JsonResponse which says if the medium was successfully returned or
     *         why it wasn't.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/return")
    public JsonResponse returnMedium(@RequestParam(value = "borrowing") long borrowingId) {
        return borrowingService.returnMedium(borrowingId);
    }

    /**
     * Returns all Borrowings with Status "REQUESTED" + the Media belonging to the
     * Borrowings
     *
     * @return Format: JSON: [ { borrwingEntity: {[object]}, medium: {[medium]} }, {
     *         borrwingEntity: {[object]}, medium: {[medium]} }, ... ]
     */
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/student/requests")
    public List<BorrowingWithMedium> getStudentRequests() {
        return borrowingService.getStudentRequests();
    }

    /**
     * Returns all Borrowings from User with Role Student and status "BORROWED" +
     * the Media belonging to the Borrowings
     *
     * @return Format: JSON: [ { borrwingEntity: {[object]}, medium: {[medium]} }, {
     *         borrwingEntity: {[object]}, medium: {[medium]} }, ... ]
     */
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/student/borrowings")
    public List<BorrowingWithMedium> getStudentBorrowings() {
        return borrowingService.getStudentBorrowings();
    }

    /**
     * Returns Borrowings from the logged in user, which are borrowed, requested or
     * declined and not older than 30 days, + the Media belonging to the Borrowings
     *
     * @return Format: JSON: [ { borrwingEntity: {[object]}, medium: {[medium]} }, {
     *         borrwingEntity: {[object]}, medium: {[medium]} }, ... ]
     */
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/borrower")
    public List<BorrowingWithMedium> getBorrowings() {
        return borrowingService.getBorrowings();
    }

    /**
     * Provides all existing physicals, with their current borrowings, belonging to
     * a specific logical medium with the id @id.
     *
     * @param id Id of a LogicalMedium
     * @return Format: JSON: [ { physical: {[object]}, borrowings: [{borrowing1},
     *         {borrowing2}, ... ] }, { physical: {[object]}, borrowings:
     *         [{borrowing1}, {borrowing2}, ... ] }, ... ]
     */
    @RequestMapping(value = "/book/{id}", method = RequestMethod.GET)
    public List<PhysicalWithBorrowings> getPhysicalsForMedium(@PathVariable("id") long id) {
        return physicalService.getPhysicalsForMedium(id);
    }

    /**
     * Returns all physicals belonging to the owner with the id @id, with their
     * current borrowings.
     *
     * @param id Id of a User
     * @return Format: JSON: [ { physical: {[object]}, borrowings: [{borrowing1},
     *         {borrowing2}, ... ] }, { physical: {[object]}, borrowings:
     *         [{borrowing1}, {borrowing2}, ... ] }, ... ]
     */
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/owner/{id}", method = RequestMethod.GET)
    public List<PhysicalWithBorrowings> getPhysicalsFromOwner(@PathVariable("id") String id) {
        return physicalService.getPhysicalsFromOwner(id);
    }

    /**
     * Returns all borrowings belonging to the physical with the id @id.
     *
     * @param id Id of a physical
     * @return Format: JSON: [{borrowing1}, {borrowing2}, ... ]
     */
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/physical/borrowings/{id}", method = RequestMethod.GET)
    public List<Borrowing> getBorrowingsFromPhysical(@PathVariable("id") String id) {
        return borrowingService.getBorrowingsFromPhysical(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/getRequested")
    public String getRequested() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(borrowingService.getRequestedBorrowings());
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResponse(500, "Error while parsing").toString();
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/borrowed")
    public String getBorrowedMediaFromUser(@RequestParam(value = "user") String userId) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(borrowingService.getBorrowedMediaFromUser(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResponse(500, "Error while parsing").toString();
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestParam(value = "borrowing_id") Long borrowingId) {

        Optional<Borrowing> optionalBorrowing = borrowingService.findBorrowing(borrowingId);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentActor = userService.findUserById(userDetails.getUsername()).get();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        if (optionalBorrowing.isPresent()) {
            Borrowing borrowing = optionalBorrowing.get();

            PdfService.ReceiptType receiptType;

            if (borrowing.getStatus().equals(BorrowingStatus.RETURNED)) {
                receiptType = PdfService.ReceiptType.RETURN_RECEIPT;
            } else if (borrowing.getStatus().equals(BorrowingStatus.BORROWED)) {
                receiptType = PdfService.ReceiptType.BORROW_RECEIPT;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        new JsonResponse(400, "Borrowing has invalid status!").toString());
            }

            byte[] pdf = pdfService.generateStudentReceipt(receiptType, borrowing, currentActor);

            return ResponseEntity.ok().headers(headers).body(pdf);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    new JsonResponse(400, "Borrowing not found!").toString());
        }
    }

    public List<Borrowing> overview() {
        return borrowingService.getBorrowingOverview();
    }

}
