package de.teamA.SWT.repository;

import de.teamA.SWT.entities.Role;
import de.teamA.SWT.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, String> {

    List<User> findAllByOrderByRoleAsc();

    Iterable<User> findAllByRole(Role role);

}
