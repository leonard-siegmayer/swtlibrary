package de.teamA.SWT.repository;

import de.teamA.SWT.entities.Wish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishRepository extends JpaRepository<Wish, Long> {

    List<Wish> findAllByOrderByRankAsc();

    List<Wish> findByIsbn(String isbn);
}
