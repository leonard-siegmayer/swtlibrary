package de.teamA.SWT.repository;

import de.teamA.SWT.entities.Person;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findByName(String name);

    boolean existsByName(String name);
}
