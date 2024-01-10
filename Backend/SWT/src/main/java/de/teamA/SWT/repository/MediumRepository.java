package de.teamA.SWT.repository;

import de.teamA.SWT.entities.Medium;
import org.springframework.data.repository.CrudRepository;

public interface MediumRepository extends CrudRepository<Medium, Long> {
    boolean existsByIsbn(String isbn);

    Medium findByIsbn(String isbn);

}
