package de.teamA.SWT.service;

import de.teamA.SWT.entities.Role;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.UserSettings;
import de.teamA.SWT.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = findUserById(username);

        if (!optionalUser.isPresent()) {
            throw new UsernameNotFoundException(username + " not found!");
        }

        User user = optionalUser.get();
        Set<GrantedAuthority> authorities = new HashSet<>();

        Role role = user.getRole();
        authorities.add(new SimpleGrantedAuthority(role.toString()));

        UserDetails springUser;
        springUser = new org.springframework.security.core.userdetails.User(user.getId(), "", authorities);

        return springUser;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> findUserById(String id) {
        return userRepository.findById(id);
    }

    public boolean setRoleForId(String userId, String role) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (!optionalUser.isPresent()) {
            return false;
        }

        User user = optionalUser.get();
        user.setRole(Role.valueOf(role));
        userRepository.save(user);
        return true;
    }

    public boolean adminExists() {
        Iterable<User> iterable = userRepository.findAllByRole(Role.ROLE_ADMIN);
        return iterable.iterator().hasNext();
    }

    public List<Role> findAllRoles() {
        return Arrays.asList(Role.values());
    }

    public List<User> findAllUsers() {
        return userRepository.findAllByOrderByRoleAsc();
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public boolean updateUserSettings(String userId, UserSettings settings) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (!optionalUser.isPresent()) {
            return false;
        }

        User user = optionalUser.get();
        user.setSettings(settings);
        userRepository.save(user);

        return true;
    }
}
