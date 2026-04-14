package com.ga.JNews.services;

import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.Newsletter;
import com.ga.JNews.models.enums.SubscriberStatus;
import com.ga.JNews.repositories.EmailRepository;
import com.ga.JNews.utilities.Uploads;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;
    private final NewsletterService newsletterService;
    private final SubscriberService subscriberService;
    private final Uploads uploads;

    public EmailService(JavaMailSender mailSender, EmailRepository emailRepository, NewsletterService newsletterService, SubscriberService subscriberService, Uploads uploads) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
        this.newsletterService = newsletterService;
        this.subscriberService = subscriberService;
        this.uploads = uploads;
    }

    public void sendNewsletterEmail(Long newsletterId, SubscriberStatus subscriberStatus) {
        Newsletter newsletter = newsletterService.getNewsletterById(newsletterId);
        if (newsletter == null) throw new InformationNotFoundException("Newsletter with id " + newsletterId + " not found");

        String[] recipients = subscriberService.getSubscribersEmailAddresses(subscriberStatus);
        if (recipients == null) throw new InformationNotFoundException("There are no subscribers.");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipients);
            helper.setSubject(newsletter.getSubject());
            helper.setText(uploads.downloadFile(newsletterService.uploadPathBodyHtml, newsletter.getBodyText()).toString(),
                    uploads.downloadFile(newsletterService.uploadPathBodyHtml, newsletter.getBodyHtml()).toString());

            mailSender.send(message);
            System.out.println("Email sent successfully");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
