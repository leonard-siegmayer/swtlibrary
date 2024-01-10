package de.teamA.SWT.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.PhysicalMedium;
import de.teamA.SWT.entities.PhysicalStatus;
import de.teamA.SWT.entities.PhysicalWithBorrowings;
import de.teamA.SWT.entities.Reservation;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.repository.PhysicalRepository;
import de.teamA.SWT.repository.ReservationRepository;

@Service
public class PhysicalService {

    private ReservationRepository reservationRepository;
    private PhysicalRepository physicalRepository;
    private AuthService authService;
    private ReservationService reservationService;
    private BorrowingService borrowingService;
    private LibraryService libraryService;

    @Autowired
    public PhysicalService(PhysicalRepository physicalRepository, ReservationRepository reservationRepository,
            AuthService authService, ReservationService reservationService, BorrowingService borrowingService,
            @Lazy LibraryService libraryService) {
        this.physicalRepository = physicalRepository;
        this.reservationRepository = reservationRepository;
        this.authService = authService;
        this.reservationService = reservationService;
        this.borrowingService = borrowingService;
        this.libraryService = libraryService;
    }

    public PhysicalMedium getPhysicalById(Long id) {
        Optional<PhysicalMedium> medium = physicalRepository.findById(id);
        if (medium.isPresent()) {
            return medium.get();
        } else {
            throw new EntityNotFoundException("PhysicalMedium with id " + id + " doesn't exist");
        }
    }

    /**
     * Deletes a physical and all corresponding reservations.
     * 
     * @param id the id of the physical
     * @return a JsonResponse including the status of the request
     */
    public JsonResponse deletePhysical(Long id) {
        PhysicalMedium physical = getPhysicalById(id);

        if (physical == null) {
            return new JsonResponse(500, "The Physical does not exist");
        }

        if (authService.isAdmin() || physical.getOwner().getId().equals(authService.getLoggedInUserName())) {
            List<Reservation> r = reservationRepository.findByPhysicalID(physical.getId());
            reservationRepository.deleteInBatch(r);
            physicalRepository.deleteInBatch(Collections.singleton(physical));
        } else {
            return new JsonResponse(500, "You can only delete your own physicals");
        }

        return new JsonResponse(200, "Physical deleted");
    }

    /**
     * Takes an already existing physical with a new status and updates it in the
     * database.
     * 
     * @param physical the physical to be updated
     * @return a JsonResponse including the status of the request
     */
    public JsonResponse updatePhysicalStatus(PhysicalMedium physical) {
        // check if the physical exists
        if (!physicalRepository.existsById(physical.getId())) {
            return new JsonResponse(500, "Physical does not exist.");
        }

        PhysicalMedium originalPhysical = physicalRepository.findById(physical.getId()).get();
        User loggedIn = this.authService.getLoggedInUser();

        // check if the user has the rights to change the physical
        if (!loggedIn.getId().equals(physical.getOwner().getId())) {
            return new JsonResponse(500, "You are not allowed to change a physical you don't own.");
        }

        // if the physical was reserved and is now unavailable, the user who was chosen
        // as the next borrower will no longer be able to borrow the book until the book
        // is available again. His rank will be set to 1 again
        if (originalPhysical.getStatus() == PhysicalStatus.RESERVED
                && physical.getStatus() == PhysicalStatus.UNAVAILABLE) {
            List<Reservation> reservations = reservationRepository.findByPhysicalID(physical.getId());
            for (Reservation r : reservations) {
                if (r.isReadyToBorrow()) {
                    reservationService.updateRanks(Integer.MAX_VALUE, 1, physical.getId(), false);
                    r.setRank("1");
                    r.setReadyToBorrow(false);
                    reservationRepository.save(r);
                    break;
                }
            }
            // if the physical was unavailable and is now reserved again, the next borrower
            // will be selected and notified
        } else if (originalPhysical.getStatus() == PhysicalStatus.UNAVAILABLE
                && physical.getStatus() == PhysicalStatus.RESERVED) {
            originalPhysical.setStatus(physical.getStatus());
            physicalRepository.save(originalPhysical);
            reservationService.selectNextBorrower(originalPhysical.getId());
        }

        originalPhysical.setStatus(physical.getStatus());
        physicalRepository.save(originalPhysical);

        return new JsonResponse(200, "Physical successfully updated.");
    }

    public List<PhysicalWithBorrowings> getPhysicalsForMedium(long id) {
        Medium medium = libraryService.getMediumById(id);

        return borrowingService.getPhysicalsWithBorrowings(medium.getPhysicals());
    }

    public List<PhysicalWithBorrowings> getPhysicalsFromOwner(String id) {
        User owner = authService.getUserById(id);
        List<PhysicalMedium> physicals = physicalRepository.findByOwner(owner);

        return borrowingService.getPhysicalsWithBorrowings(physicals);
    }
}
