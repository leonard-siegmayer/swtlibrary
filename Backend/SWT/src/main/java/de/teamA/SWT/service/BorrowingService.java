package de.teamA.SWT.service;

import static de.teamA.SWT.util.DateUtil.daysBetween;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import de.teamA.SWT.entities.Borrowing;
import de.teamA.SWT.entities.BorrowingStatus;
import de.teamA.SWT.entities.BorrowingWithMedium;
import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.PhysicalMedium;
import de.teamA.SWT.entities.PhysicalStatus;
import de.teamA.SWT.entities.PhysicalWithBorrowings;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.repository.BorrowingRepository;
import de.teamA.SWT.repository.MediumRepositoryWrapper;
import de.teamA.SWT.repository.MediumRepositoryWrapperException;
import de.teamA.SWT.repository.PhysicalRepository;
import de.teamA.SWT.service.email.EmailService;

@Service
public class BorrowingService {

    @Value("${borrowings.max_days}")
    private int MAX_DAYS;

    private MediumRepositoryWrapper mediumRepositoryWrapper;
    private BorrowingRepository borrowingRepository;
    private PhysicalRepository physicalRepository;
    private EmailService emailService;
    private AuthService authService;
    private ReservationService reservationService;
    private PhysicalService physicalService;
    private LibraryService libraryService;

    @Autowired
    public BorrowingService(MediumRepositoryWrapper mediumRepositoryWrapper, BorrowingRepository borrowingRepository,
            PhysicalRepository physicalRepository, EmailService emailService, AuthService authService,
            @Lazy ReservationService reservationService, @Lazy PhysicalService physicalService, @Lazy LibraryService libraryService) {
        this.mediumRepositoryWrapper = mediumRepositoryWrapper;
        this.borrowingRepository = borrowingRepository;
        this.physicalRepository = physicalRepository;
        this.emailService = emailService;
        this.authService = authService;
        this.libraryService = libraryService;
        this.physicalService = physicalService;
        this.reservationService = reservationService;
    }

    private Borrowing getBorrowingById(Long id) {
        Optional<Borrowing> borrowing = borrowingRepository.findById(id);
        if (borrowing.isPresent()) {
            return borrowing.get();
        } else {
            throw new EntityNotFoundException("Borrowing with id " + id + " doesn't exist");
        }
    }

    public JsonResponse borrow(LocalDate dueDate, long mediumId) throws ValidationException {
        if (daysBetween(dueDate, LocalDate.now()) > MAX_DAYS) {
            return new JsonResponse(500, "Maximal borrow duration is " + MAX_DAYS + " days.");
        }

        try {
            PhysicalMedium physical = physicalService.getPhysicalById(mediumId);
            Borrowing borrowing = new Borrowing(this.authService.getLoggedInUser(), physical, dueDate);

            borrowingRepository.save(borrowing);
            physical.setStatus(PhysicalStatus.BORROWED);
            physicalRepository.save(physical);
            
            emailService.sendBorrowInfo(borrowing);

            Medium medium = mediumRepositoryWrapper.get(physical.getMediumId()).get();
            if (medium.getBorrowDate() == null) {
                medium.setBorrowDate(new ArrayList<>());
            }

            List<LocalDate> borrowDates = medium.getBorrowDate();
            borrowDates.add(borrowing.getBorrowDate());
            medium.setBorrowDate(borrowDates);

            try {
                mediumRepositoryWrapper.update(medium);
            } catch (MediumRepositoryWrapperException e) {
                System.err.println(e);
            }
            return new JsonResponse(200, "Book successfully borrowed");
        } catch (Exception e) {
            return new JsonResponse(500, e.getMessage());
        }

    }

    public JsonResponse borrowStudent(long mediumId) {
        try {
            Borrowing borrowing = new Borrowing(this.authService.getLoggedInUser(),
                    physicalService.getPhysicalById(mediumId), null, true);

            borrowingRepository.save(borrowing);
            emailService.sendBorrowInfo(borrowing);
            return new JsonResponse(200, "Book successfully requested");

        } catch (Exception e) {
            return new JsonResponse(500, e.getMessage());
        }

    }

    public JsonResponse borrowForStudent(LocalDate dueDate, String userId, long mediumId) throws ValidationException {

        if (daysBetween(dueDate, LocalDate.now()) > MAX_DAYS) {
            return new JsonResponse(500, "Maximal borrow duration is " + MAX_DAYS + " days.");
        }

        try {
            User student = authService.getUserById(userId);
            PhysicalMedium physical = physicalService.getPhysicalById(mediumId);

            Borrowing borrowing = new Borrowing(student, physical, null, true);
            borrowing.accept(this.authService.getLoggedInUser(), dueDate);

            physical.setStatus(PhysicalStatus.BORROWED);

            physicalRepository.save(physical);
            borrowingRepository.save(borrowing);
            return new JsonResponse(200, "Book successfully borrowed for student");

        } catch (Exception e) {
            return new JsonResponse(500, e.getMessage());
        }
    }

    public JsonResponse returnMedium(Long borrowingId) {

        Borrowing borrowing;

        try {
            borrowing = getBorrowingById(borrowingId);
        } catch (EntityNotFoundException e) {
            return new JsonResponse(500, e.getMessage());
        }

        if (borrowing.getReturnDate() == null | borrowing.getStatus() != BorrowingStatus.RETURNED) {
            borrowing.setReturnDate(LocalDate.now());
            borrowing.setStatus(BorrowingStatus.RETURNED);

            borrowingRepository.save(borrowing);
            if (borrowing.getPhysical().getStatus() != PhysicalStatus.UNAVAILABLE) {
                reservationService.selectNextBorrower(borrowing.getPhysical().getId());
            }
            emailService.sendReturnInfo(borrowing);
            return new JsonResponse(200, "Medium returned");
        } else {
            return new JsonResponse(500, "Medium is already returned");
        }
    }

    public Iterable<Borrowing> getBorrowedMediaFromUser(String userId) {
        return borrowingRepository.findByBorrower(authService.getUserById(userId));
    }

    public JsonResponse acceptBorrowing(Long borrowingId, LocalDate dueDate) throws ValidationException {

        if (daysBetween(dueDate, LocalDate.now()) > MAX_DAYS) {
            return new JsonResponse(500, "Maximal borrow duration is " + MAX_DAYS + " days.");
        }

        Borrowing borrowing;
        User staff;
        try {
            borrowing = getBorrowingById(borrowingId);

            staff = this.authService.getLoggedInUser();
        } catch (EntityNotFoundException e) {
            return new JsonResponse(500, e.getMessage());
        }

        if (borrowing.getStatus() == BorrowingStatus.REQUESTED) {
            borrowing.accept(staff, dueDate);
            borrowingRepository.save(borrowing);

            PhysicalMedium physical = borrowing.getPhysical();
            physical.setStatus(PhysicalStatus.BORROWED);
            physicalRepository.save(physical);
            return new JsonResponse(200, "Borrowing is accepted");
        } else {
            return new JsonResponse(500, "This borrowing is not requested");
        }
    }

    public JsonResponse declineBorrowing(Long borrowingId) {
        Borrowing borrowing;
        User staff;

        try {
            borrowing = getBorrowingById(borrowingId);

            staff = this.authService.getLoggedInUser();

        } catch (EntityNotFoundException e) {
            return new JsonResponse(500, e.getMessage());
        }
        if (borrowing.getStatus() == BorrowingStatus.REQUESTED) {
            borrowing.decline(staff);

            borrowingRepository.save(borrowing);
            return new JsonResponse(200, "Borrowing is declined");
        } else {
            return new JsonResponse(500, "This borrowing is not requested");
        }
    }

    public Iterable<Borrowing> getRequestedBorrowings() {
        return borrowingRepository.findByStatus(BorrowingStatus.REQUESTED);
    }

    public List<Borrowing> getBorrowingsFromPhysical(String id) {
        PhysicalMedium physical = physicalService.getPhysicalById(Long.parseLong(id));
        return borrowingRepository.findByMedium(physical);
    }

    /**
     * Returns all Borrowings with Status "REQUESTED" + the Media belonging to the
     * Borrowings
     **/
    public List<BorrowingWithMedium> getStudentRequests() {

        List<Borrowing> requests = borrowingRepository.findByStatus(BorrowingStatus.REQUESTED);

        return getBorrowingsWithMedia(requests);

    }

    public JsonResponse extendBorrowing(long borrowingId, LocalDate dueDate) throws ValidationException {

        Borrowing borrowing = getBorrowingById(borrowingId);
        LocalDate borrowDate = borrowing.getBorrowDate();

        if (daysBetween(dueDate, borrowDate) > MAX_DAYS) {
            return new JsonResponse(500, "Maximal borrow duration is " + MAX_DAYS + " days.");
        }

        borrowing.setDueDate(dueDate);

        if (borrowingRepository.save(borrowing) != null) {
            emailService.sendExtensionInfo(borrowing);
            return new JsonResponse(200, "Borrowing date extended to " + dueDate);
        }

        return new JsonResponse(500, "Error while saving Borrowing");
    }

    public List<BorrowingWithMedium> getBorrowings() {

        List<Borrowing> borrowings = borrowingRepository.getBorrowingsFromUser(this.authService.getLoggedInUser());

        System.err.println(borrowings.size());

        return getBorrowingsWithMedia(borrowings);
    }

    public List<BorrowingWithMedium> getStudentBorrowings() {

        List<Borrowing> borrowings = borrowingRepository.getBorrowingsFromStudents();

        return getBorrowingsWithMedia(borrowings);
    }

    public List<PhysicalWithBorrowings> getPhysicalsWithBorrowings(Collection<PhysicalMedium> physicals) {
        List<PhysicalWithBorrowings> result = new ArrayList<>();

        for (PhysicalMedium physical : physicals) {

            PhysicalWithBorrowings physicalWithBorrowings = new PhysicalWithBorrowings();
            physicalWithBorrowings.physical = physical;

            physicalWithBorrowings.borrowings = borrowingRepository.findByMediumAndReturnDateIsNull(physical);

            result.add(physicalWithBorrowings);
        }

        return result;
    }

    public List<BorrowingWithMedium> getBorrowingsWithMedia(Iterable<Borrowing> borrowings) {
        List<BorrowingWithMedium> result = new ArrayList<>();

        for (Borrowing request : borrowings) {
            BorrowingWithMedium borrowingMedium = new BorrowingWithMedium();
            borrowingMedium.borrowingEntity = request;
            borrowingMedium.medium = libraryService.getMediumById(request.getPhysical().getMediumId());

            result.add(borrowingMedium);
        }

        return result;
    }

    public Optional<Borrowing> findBorrowing(Long borrowingId) {
        return borrowingRepository.findById(borrowingId);
    }

    public List<Borrowing> getBorrowingOverview() {
        return borrowingRepository.findAllByOrderByDueDateDesc();
    }
}
