package de.teamA.SWT.repository;

import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.PhysicalMedium;
import de.teamA.SWT.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhysicalRepository extends JpaRepository<PhysicalMedium, Long> {

    List<PhysicalMedium> findByOwner(User user);

    List<PhysicalMedium> findAllByMedium(Medium medium);

}
