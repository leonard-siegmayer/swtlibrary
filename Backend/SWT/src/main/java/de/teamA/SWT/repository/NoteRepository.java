package de.teamA.SWT.repository;

import de.teamA.SWT.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {

}
