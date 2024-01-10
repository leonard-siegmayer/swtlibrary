package de.teamA.SWT.entities.reqres;

import de.teamA.SWT.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Value;

public class ConfigResponse {

    @Value("${borrowings.max_days}")
    public int MAX_DAYS;

    @Value("${expiration.days}")
    public int EXP_DAYS;

    @Value("${borrowing.max_extDays}")
    public int EXT_DAYS;

    @Value("${overdue.days}")
    public int OVERDUE_DAYS;

    public String[] publicEndpoints;

    public ConfigResponse(int maxDays, int expDays, int overdueDays) {
        MAX_DAYS = maxDays;
        EXP_DAYS = expDays;
        OVERDUE_DAYS = overdueDays;
        publicEndpoints = SecurityConfig.publicAvailEndpoints;

    }

}
