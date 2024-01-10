INSERT INTO user (user_id, email, name, profile_picture, role, settings) VALUES
    (-9223372036854775808, "defaultuser@example.com", "Default Library", "", "ROLE_STAFF", NULL)
    ON DUPLICATE KEY UPDATE user_id = -9223372036854775808
;
