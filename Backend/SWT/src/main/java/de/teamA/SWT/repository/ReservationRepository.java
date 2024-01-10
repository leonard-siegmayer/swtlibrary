package de.teamA.SWT.repository;

import de.teamA.SWT.entities.Reservation;
import de.teamA.SWT.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser(User user);

    List<Reservation> findByPhysicalID(Long physicalID);

}
