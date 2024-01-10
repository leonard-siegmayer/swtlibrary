package de.teamA.SWT.controller;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.teamA.SWT.entities.Reservation;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.service.AuthService;
import de.teamA.SWT.service.ReservationService;

@RestController
@RequestMapping("api/reservation")
@CrossOrigin
public class ReservationController {

    @Autowired
    AuthService authService;

    @Autowired
    ReservationService reservationService;

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN')")
    @GetMapping(value = "/user/{id}")
    public List<Reservation> getReservationsByUser(@PathVariable(value = "id") String userId) {
        User user = authService.getUserById(userId);
        return reservationService.getReservationsByUser(user);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN')")
    @GetMapping(value = "/medium/{id}")
    public List<Reservation> getReservationsBy(@PathVariable(value = "id") Long physicalID) {
        return reservationService.getReservationsByPhysicalID(physicalID);
    }

    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN')")
    @PostMapping
    public Reservation saveReservation(@RequestBody Reservation reservation) {
        return reservationService.saveReservation(reservation);
    }

    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN')")
    @DeleteMapping(value = "/{id}")
    public JsonResponse deleteReservation(@PathVariable(value = "id") Long id) {
        return reservationService.deleteReservation(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN')")
    @PostMapping(value = "/update/{id}/{rank}")
    public JsonResponse updateReservationRank(@PathVariable(value = "id") long reservationId,
            @PathVariable(value = "rank") String rank) {
        return reservationService.updateReservationRank(reservationId, rank);
    }

    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_STAFF', 'ROLE_ADMIN')")
    @GetMapping(value = "/redeem/{id}")
    public JsonResponse redeemReservation(@PathVariable Long id) {
        return reservationService.redeemReservation(id);
    }
}
