package de.teamA.SWT.repository;

import de.teamA.SWT.entities.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

}
