package de.teamA.SWT.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.PhysicalMedium;
import de.teamA.SWT.entities.PhysicalStatus;
import de.teamA.SWT.entities.Reservation;
import de.teamA.SWT.entities.Role;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.repository.MediumRepositoryWrapper;
import de.teamA.SWT.repository.MediumRepositoryWrapperException;
import de.teamA.SWT.repository.PhysicalRepository;
import de.teamA.SWT.repository.ReservationRepository;
import de.teamA.SWT.service.email.EmailService;
import de.teamA.SWT.service.email.EmailServiceException;

@Service
public class ReservationService {

    @Value("${reservation.available.max_hours}")
    private int AVAILABLE_RESERVATION_MAX_HOURS;

    private ReservationRepository reservationRepository;
    private EmailService emailService;
    private PhysicalRepository physicalRepository;
    private MediumRepositoryWrapper mediumRepositoryWrapper;
    private AuthService authService;
    private BorrowingService borrowingService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, EmailService emailService,
            PhysicalRepository physicalRepository, MediumRepositoryWrapper mediumRepositoryWrapper,
            AuthService authService, @Lazy BorrowingService borrowingService) {
        this.reservationRepository = reservationRepository;
        this.emailService = emailService;
        this.physicalRepository = physicalRepository;
        this.mediumRepositoryWrapper = mediumRepositoryWrapper;
        this.authService = authService;
        this.borrowingService = borrowingService;

    }

    /**
     * Returns all open reservations of a user.
     * 
     * @param user the user
     * @return a list containing the reservations
     */
    public List<Reservation> getReservationsByUser(User user) {
        List<Reservation> result = this.reservationRepository.findByUser(user);
        result.removeIf(r -> r.getBorrowed());
        return result;
    }

    /**
     * Returns all open reservations of a physical
     * 
     * @param physicalID the id of the physical
     * @return a list conaining all the reservations
     */
    public List<Reservation> getReservationsByPhysicalID(long physicalID) {
        List<Reservation> result = this.reservationRepository.findByPhysicalID(physicalID);
        result.removeIf(r -> r.getBorrowed());
        return result;
    }

    public Reservation saveReservation(Reservation reservation) {

        Optional<Medium> optional = mediumRepositoryWrapper.get(reservation.getLogicalID());
        if (!optional.isPresent()) {
            return null;
        }

        Reservation result = reservationRepository.save(reservation);
        Medium medium = optional.get();
        if (medium.getReservation() == null) {
            medium.setReservation(new HashSet<>());
        }

        Set<Reservation> reservations = medium.getReservation();
        reservations.add(result);
        medium.setReservation(reservations);

        // Date for the Numbers of Reservation and for TopAndFlop statistic
        medium.getResDate().add(result.getDate().toLocalDate());

        try {
            mediumRepositoryWrapper.update(medium);
        } catch (MediumRepositoryWrapperException e) {
            // TODO: maybe let this method throw an exception
            System.err.println(e);
        }

        PhysicalMedium phyMedi = physicalRepository.findById(reservation.getPhysicalID()).get();
        int previousValue = phyMedi.getResCount() == null ? 0 : phyMedi.getResCount();
        phyMedi.setResCount(previousValue + 1);
        physicalRepository.save(phyMedi);
        return result;
    }

    public Optional<Reservation> getReservationById(Long id) {
        return this.reservationRepository.findById(id);
    }

    public JsonResponse deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            return new JsonResponse(500, "Reservation with id " + id + " doesn't exist");
        }

        Reservation reservation = reservationRepository.findById(id).get();
        PhysicalMedium physical = physicalRepository.findById(reservation.getPhysicalID()).get();
        // check rights
        User loggedIn = this.authService.getLoggedInUser();
        if (!hasRightsForReservation(reservation, loggedIn)) {
            return new JsonResponse(500, "Missing rights for the reservation.");
        }

        if (reservation.isReadyToBorrow()) {
            selectNextBorrower(reservation.getPhysicalID());
        } else if (reservation.getRank() != null) {
            this.updateRanks(Integer.parseInt(reservation.getRank()), 0, reservation.getPhysicalID(), true);
        }

        Reservation r = reservationRepository.findById(id).get();
        r.setBorrowDate(LocalDateTime.now());
        r.setBorrowed(true);
        reservationRepository.save(r);

        if (physical.getStatus() == PhysicalStatus.RESERVED) {
            List<Reservation> reservationListForPhysical = reservationRepository.findByPhysicalID(physical.getId());
            reservationListForPhysical.removeIf(res -> res.getBorrowed() == true);
            if (reservationListForPhysical.isEmpty()) {
                physical.setStatus(PhysicalStatus.AVAILABLE);
                physicalRepository.save(physical);
            }
        }
        PhysicalMedium phyMedi = physicalRepository.findById(reservation.getPhysicalID()).get();
        phyMedi.decrementResCount();
        physicalRepository.save(phyMedi);
        return new JsonResponse(200, "Reservation deleted");
    }

    /**
     * updates the rank of a reservation. If the new rank is 0, the rank will be
     * deleted.
     * 
     * @param reservationId the id of the reservation
     * @param rank          the new rank
     * @return a JsonResponse with the status of the request
     */
    public JsonResponse updateReservationRank(long reservationId, String rank) {

        if (!this.reservationRepository.existsById(reservationId)) {
            return new JsonResponse(500, "The reservation does not exist");
        }

        User user = this.authService.getLoggedInUser();
        Reservation reservation = this.reservationRepository.findById(reservationId).get();

        // check if user has the rights to change the reservations
        if (!hasRightsForReservation(reservation, user)) {
            return new JsonResponse(500, "Missing rights for the reservation.");
        }

        boolean rankNotSet = reservation.isRankSet();
        int oldRank = rankNotSet ? Integer.MAX_VALUE : Integer.parseInt(reservation.getRank());

        updateRanks(oldRank, Integer.parseInt(rank), reservation.getPhysicalID(), false);

        if (rankNotSet) {
            reservation.setRank(rank);
            reservationRepository.save(reservation);
        }

        return new JsonResponse(200, "Reservation updated");
    }

    public JsonResponse redeemReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            return new JsonResponse(500, "Reservation does not exist");
        }
        Reservation reservation = reservationRepository.findById(id).get();

        // check rights
        User loggedIn = this.authService.getLoggedInUser();
        if (!hasRightsForReservation(reservation, loggedIn)) {
            return new JsonResponse(500, "Missing rights for the reservation.");
        }

        // check if reservation can be redeemed
        if (!reservation.isReadyToBorrow()) {
            return new JsonResponse(500, "Reservation can not yet ready to be redeemed.");
        }

        JsonResponse borrowResponse = borrowingService.borrow(
                LocalDate.now(ZoneOffset.UTC).plusDays(reservation.getRequiredTime()), reservation.getPhysicalID());

        if (borrowResponse.status == 500) {
            return new JsonResponse(500, "Reservation could not be redeemed.");
        }

        Reservation r = reservationRepository.findById(id).get();
        r.setBorrowed(true);

        if (r.getRank() != null) {
            updateRanks(Integer.parseInt(r.getRank()), 0, r.getPhysicalID(), true);
        }

        reservationRepository.save(r);
        PhysicalMedium phyMedi = physicalRepository.findById(reservation.getPhysicalID()).get();
        phyMedi.decrementResCount();
        physicalRepository.save(phyMedi);
        return new JsonResponse(200, "Reservation redeemed.");
    }

    /**
     * After a borrowing has been returned, the next borrower is selected based on
     * the list of reservations.
     * 
     * @param physicalId the id of the physical for which the next borrowing has to
     *                   be selected
     */
    public void selectNextBorrower(Long physicalId) {
        List<Reservation> reservations = getReservationsByPhysicalID(physicalId);
        PhysicalMedium physical = physicalRepository.getOne(physicalId);
        if (reservations.size() < 1 || physical.getStatus() == PhysicalStatus.UNAVAILABLE) {
            if (physical != null && physical.getStatus() != PhysicalStatus.UNAVAILABLE) {
                physical.setStatus(PhysicalStatus.AVAILABLE);
                physicalRepository.save(physical);
            }
            return;
        }
        // find the next borrowing based on the following criteria:
        // 1. Rank 2. Priority 3. Time
        Reservation next = null;
        int highestPriority = reservations.get(0).priority;

        for (Reservation r : reservations) {
            if (r.priority < highestPriority) {
                highestPriority = r.priority;
            }
            // a rank has been assigned
            if (r.getRank() != null) {
                if (r.hasMaxRank()) {
                    next = r;
                    break;
                }
            }
        }

        // no rank has been assigend => select highest priority
        if (next == null) {
            final int priority = highestPriority;
            // remove all reservations with a rank lower than the highest rank
            reservations.removeIf(r -> r.getPriority() > priority);
            reservations.sort((a, b) -> a.getDate().compareTo(b.getDate()));
            next = reservations.get(0);
        }

        // set reservation readyToBorrow to true and notify user
        next.setReadyToBorrow(true);
        next.setBorrowDate(LocalDateTime.now());
        reservationRepository.save(next);
        if (next.getRank() != null) {
            updateRanks(Integer.parseInt(next.getRank()), 0, physicalId, true);
        }
        if (physical != null) {
            physical.setStatus(PhysicalStatus.RESERVED);
            physicalRepository.save(physical);
        }

        try {
            emailService.sendReadyToBorrowInfo(next, physical);
        } catch (EmailServiceException e) {
            // TODO: maybe handle this exception
            System.err.println(e);
        }

        // delete reservation after specified time
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final Reservation finalReservation = next;
        Runnable task = new Runnable() {
            public void run() {
                if (reservationRepository.findById(finalReservation.getId()).isPresent()) {
                    finalReservation.setBorrowed(true);
                    finalReservation.setBorrowDate(LocalDateTime.now());
                    reservationRepository.save(finalReservation);
                    selectNextBorrower(physicalId);
                    try {
                        emailService.sendMissedToBorrow(finalReservation, physical);
                    } catch (EmailServiceException e) {
                        System.err.println(e);
                    }
                }
                ;
            }
        };

        scheduler.schedule(task, AVAILABLE_RESERVATION_MAX_HOURS, TimeUnit.HOURS);
        scheduler.shutdown();
    }

    /**
     * If the rank of a reservation is changed, this method can be used to update
     * the ranks of the other reservations of the physical
     * 
     * @param oldRank    the original rank of the reservation
     * @param newRank    the new rank of the reservation. Set it to 0 if the
     *                   reservation is deleted.
     * @param physicalId the id of the physical
     * @param delete     true if the rank has not been changed, but the reservation
     *                   was completely deleted
     */
    public void updateRanks(int oldRank, int newRank, Long physicalId, boolean delete) {
        if (oldRank == newRank) {
            return;
        }

        List<Reservation> reservations = reservationRepository.findByPhysicalID(physicalId);
        reservations.removeIf(r -> r.getRank() == null);
        for (Reservation r : reservations) {
            if (r.getRank().isEmpty()) {
                continue;
            }
            r.updateRank(oldRank, newRank, delete);
            reservationRepository.save(r);
        }
    }

    /**
     * Returns true if the given user is either admin or if she/he made the given
     * reservation
     * 
     * @param r    the reservation to check
     * @param user the user to check for
     * @return true if user is admin or owns the reservation r
     */
    private boolean hasRightsForReservation(Reservation r, User user) {
        Optional<PhysicalMedium> pm = physicalRepository.findById(r.getPhysicalID());
        boolean isOwner = pm.get().getOwner() == user;
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        return isOwner || isAdmin;
    }
}