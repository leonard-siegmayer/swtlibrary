package de.teamA.SWT.repository;

import de.teamA.SWT.entities.EmailEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface EmailRepository extends CrudRepository<EmailEntry, Long> {

    List<EmailEntry> findAllByType(String mailType);

    List<EmailEntry> findByRecipientAndType(String receiver, String mailType);

    @Transactional
    List<EmailEntry> deleteByDateBefore(Date date);

}
