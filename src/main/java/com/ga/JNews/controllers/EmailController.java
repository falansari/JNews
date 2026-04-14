package com.ga.JNews.controllers;

import com.ga.JNews.models.enums.SubscriberStatus;
import com.ga.JNews.services.EmailService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "emails")
public class EmailController {
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public void sendEmail(@RequestParam("newsletterId") Long newsletterId, @RequestParam("subscriberStatus") SubscriberStatus subscriberStatus) {
        emailService.sendNewsletterEmail(newsletterId, subscriberStatus);
    }
}
