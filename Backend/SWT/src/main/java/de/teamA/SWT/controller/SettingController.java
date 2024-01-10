package de.teamA.SWT.controller;

import de.teamA.SWT.entities.reqres.ConfigResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/configs")
@CrossOrigin
public class SettingController {

    @Value("${borrowings.max_days}")
    private int MAX_DAYS;

    @Value("${expiration.days}")
    private int EXP_DAYS;

    @Value("${overdue.days}")
    private int OVERDUE_DAYS;

    @RequestMapping(method = RequestMethod.GET)
    public ConfigResponse configs() {
        ConfigResponse response = new ConfigResponse(MAX_DAYS, EXP_DAYS, OVERDUE_DAYS);
        return response;

    }
}
