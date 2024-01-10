package de.teamA.SWT.repository;

import de.teamA.SWT.entities.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    List<Keyword> findByName(String name);

    boolean existsByName(String name);
}
