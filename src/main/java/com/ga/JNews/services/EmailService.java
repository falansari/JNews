package com.ga.JNews.services;

import com.ga.JNews.exceptions.BadRequestException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.Newsletter;
import com.ga.JNews.models.Subscriber;
import com.ga.JNews.models.enums.SubscriberStatus;
import com.ga.JNews.repositories.EmailRepository;
import com.ga.JNews.utilities.Uploads;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

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

        String newsletterHtml;
        String newsletterText;

        try { // Retrieve newsletter's HTML and plain text bodies from server
            newsletterHtml = Objects.requireNonNull(uploads.downloadFile(newsletterService.uploadPathBodyHtml,
                    newsletter.getBodyHtml()).getBody()).getContentAsString(StandardCharsets.UTF_8)
                    .replace("{{title}}", newsletter.getTitle()); // Replace newsletter's {{title}}
            newsletterText = Objects.requireNonNull(uploads.downloadFile(newsletterService.uploadPathBodyText,
                    newsletter.getBodyText()).getBody()).getContentAsString(StandardCharsets.UTF_8)
                    .replace("{{title}}", newsletter.getTitle());
        } catch (IOException e) {
            throw new BadRequestException("Error while retrieving newsletter bodies");
        }

        ArrayList<Subscriber> recipients = subscriberService.getSubscribersByStatus(subscriberStatus);
        if (recipients == null) throw new InformationNotFoundException("There are no recipients");

        // TODO: make the operation Asynchronous
        for (Subscriber subscriber : recipients) {
            try {
                // Replace newsletter's {{subscriber}} placeholder
                String customizedHtml = newsletterHtml.replace("{{subscriber}}", subscriber.getName());
                String customizedText = newsletterText.replace("{{subscriber}}", subscriber.getName());

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(subscriber.getEmail());
                helper.setSubject(newsletter.getSubject());
                helper.setText(customizedText, customizedHtml);

                mailSender.send(message);

                // TODO: save sent e-mail records to db
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
