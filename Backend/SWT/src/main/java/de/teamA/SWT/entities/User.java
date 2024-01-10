package de.teamA.SWT.entities;

import de.teamA.SWT.entities.UserSettings;
import javax.persistence.*;

@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(name = "user_id", length = 25)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "profile_picture")
    private String profilePicture;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "settings")
    private UserSettings settings = new UserSettings();

    public User() {
        this.settings = new UserSettings();
    }

    public User(String id, String name, String email, Role roleUser, String profilePicture) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = roleUser;
        this.profilePicture = profilePicture;
        this.settings = new UserSettings();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public UserSettings getSettings() {
        return settings;
    }

    public void setSettings(UserSettings settings) {
        if (settings == null) {
            this.settings = new UserSettings();
        } else {
            this.settings = settings;
        }
    }

    @Override
    public String toString() {
        return "ID: " + id + ", \n Name: " + name;
    }
}
