package de.teamA.SWT.controller;

import de.teamA.SWT.entities.Role;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.UserSettings;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/user")
@CrossOrigin
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    SessionRegistry sessionRegistry;

    @PreAuthorize("hasRole('ROLE_ADMIN', 'ROLE_STAFF')")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User getUser(@PathVariable("id") String id) {
        Optional<User> userOptional = userService.findUserById(id);

        if (!userOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found!");
        }

        return userOptional.get();
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    // Use with care
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable("id") String id) {
        userService.deleteUser(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN', 'ROLE_STAFF')")
    @RequestMapping(value = "/getRoles", method = RequestMethod.GET)
    public List<String> getAllRoles() {
        return userService.findAllRoles().stream().map(Role::getSimpleName).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public String setRole(@PathVariable("id") final String id, @RequestParam("setRole") String role) {

        UserDetails actingUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (actingUser.getUsername().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An admin cannot change his own role!");
        }

        if (!userService.setRoleForId(id, role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found!");
        }

        Optional<Object> userWithRoleChangeOptional = sessionRegistry.getAllPrincipals().stream()
                .filter(user -> ((UserDetails) user).getUsername().equals(id)).findFirst();

        if (userWithRoleChangeOptional.isPresent()) {
            Object userWithRoleChange = userWithRoleChangeOptional.get();
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(userWithRoleChange, false);

            for (SessionInformation session : sessions) {
                session.expireNow();
            }
        }

        return "{ \"message\": \"Success!\"}";
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN', 'ROLE_STUDENT')")
    @RequestMapping(value = "/{id}/settings", method = RequestMethod.POST)
    public JsonResponse updateUserSettings(@PathVariable("id") final String id, @RequestBody UserSettings settings) {
        if (userService.updateUserSettings(id, settings)) {
            return new JsonResponse(200, "Settings successfully updated!");
        } else {
            return new JsonResponse(500, "Updating settings failed.");
        }
    }

}
