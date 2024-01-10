package de.teamA.SWT.controller;

import de.teamA.SWT.entities.User;
import de.teamA.SWT.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@RestController
@RequestMapping(value = "auth")
@CrossOrigin
public class AuthController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<String> login(Principal principal) {
        if (principal != null) {
            return ResponseEntity.ok("{\"message\": \"Logged in.\"}");
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Login failed!");
        }
    }

    @RequestMapping(value = "/whoami", method = RequestMethod.GET)
    public User whoami() {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authenticated!");
        }

        String id = userDetails.getUsername();
        Optional<User> user = userService.findUserById(id);

        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }

        return user.get();

    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity<String> logout(HttpSession session) {
        LocalDateTime expirationDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        session.setAttribute("Expires", expirationDate.toString());
        session.invalidate();
        return ResponseEntity.ok("{ \"message\": \"Logged out.\"}");
    }

}
