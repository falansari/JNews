package com.ga.JNews.services;

import com.ga.JNews.exceptions.BadRequestException;
import com.ga.JNews.exceptions.InformationExistException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.Newsletter;
import com.ga.JNews.repositories.NewsletterRepository;
import com.ga.JNews.utilities.Uploads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class NewsletterService {
    private final NewsletterRepository newsletterRepository;
    private final Uploads uploads;
    private final String uploadPathBodyHtml = "uploads/newsletters/bodyHtml";
    private final String uploadPathBodyText = "uploads/newsletters/bodyText";

    @Autowired
    public NewsletterService(NewsletterRepository newsletterRepository, Uploads uploads) {
        this.newsletterRepository = newsletterRepository;
        this.uploads = uploads;
    }

    /**
     * Get newsletter by their database ID.
     * @param id Long
     * @return Newsletter
     * @exception InformationNotFoundException Doesn't exist
     */
    public Newsletter getNewsletterById(Long id){
        return newsletterRepository.findById(id).orElseThrow(() -> new InformationNotFoundException("Newsletter with id " + id + " not found"));
    }

    /**
     * Get newsletter by its title.
     * @param title String
     * @return Newsletter
     * @exception InformationNotFoundException Doesn't exist
     */
    public Newsletter getNewsletterByTitle(String title){
        Newsletter newsletter = newsletterRepository.findByTitle(title);

        if (newsletter == null) throw new InformationNotFoundException("Newsletter with title " + title + " not found");

        return newsletter;
    }

    /**
     * Get list of all newsletters from database. Asynchronous operation.
     * @return CompletableFuture ArrayList Newsletter
     */
    public CompletableFuture<ArrayList<Newsletter>> getNewsletters() {
        return newsletterRepository.findAllBy();
    }

    /**
     * Create a new newsletter and save to storage. Accepts JSON object.
     * @param title String Newsletter's unique title name
     * @param subject String Newsletter's email subject line
     * @param bodyHtml MultipartFile Newsletter's .html formatted HTML body file
     * @param bodyText MultipartFile Newsletter's .txt plain text body file, used as backup when HTML is rejected.
     * @return Newsletter
     * @exception InformationExistException Newsletter title already exists.
     * @exception BadRequestException Missing data, all fields required.
     * @apiNote String title, String subject, String bodyHtml (full .html document), String bodyText (plain-text fallback version)
     */
    public Newsletter createNewsletter(String title, String subject, MultipartFile bodyHtml, MultipartFile bodyText) {
        if (newsletterRepository.existsByTitle(title)) {
            throw new InformationExistException("Newsletter with title " + title + " already exists");
        }

        String uploadedBodyHtml = uploadNewsletterBodyHtml(bodyHtml);
        String uploadedBodyText = uploadNewsletterBodyText(bodyText);

        Newsletter newsletter = new Newsletter();
        newsletter.setTitle(title);
        newsletter.setSubject(subject);
        newsletter.setBodyHtml(uploadedBodyHtml);
        newsletter.setBodyText(uploadedBodyText);

        return newsletterRepository.save(newsletter);
    }

    /**
     * Update Newsletter's data by its id.
     * @param id Long Existing newsletter's database ID
     * @param title String Newsletter's unique title name. If null or already exists in db will be skipped.
     * @param subject String Newsletter's email subject line. If null will be skipped.
     * @param bodyHtml MultipartFile Newsletter's .html formatted HTML body file. If empty will be skipped.
     * @param bodyText MultipartFile Newsletter's .txt plain text body file, used as backup when HTML is rejected. If empty will be skipped.
     * @return Newsletter updated
     * @exception InformationNotFoundException Newsletter ID doesn't exist
     * @apiNote Long id, String title, String subject, String bodyHtml (full .html document), String bodyText (plain-text fallback version)
     */
    public Newsletter updateNewsletter(Long id, String title, String subject, MultipartFile bodyHtml, MultipartFile bodyText) {
        Newsletter storedNewsletter = getNewsletterById(id);

        if (storedNewsletter == null) throw new InformationNotFoundException("Newsletter with id " + id + " not found");

        // update title
        if (title != null && !title.equals(storedNewsletter.getTitle())) {
            if (!newsletterRepository.existsByTitle(title)) {
                storedNewsletter.setTitle(title);
            }
        }

        // update subject
        if (subject != null && !subject.equals(storedNewsletter.getSubject())) {
            storedNewsletter.setSubject(subject);
        }

        // update body_html
        if (!bodyHtml.isEmpty()) {
            String uploadedBodyHtml = uploadNewsletterBodyHtml(bodyHtml);

            if (!uploadedBodyHtml.isEmpty()) { // only update records if upload was successful
                uploads.deleteFile(uploadPathBodyHtml, storedNewsletter.getBodyHtml()); // delete old file
                storedNewsletter.setBodyHtml(uploadedBodyHtml);
            }
        }

        // update body_text
        if (!bodyText.isEmpty()) {
            String uploadedBodyText = uploadNewsletterBodyText(bodyText);

            if (!uploadedBodyText.isEmpty()) { // Only update records if upload was successful
                uploads.deleteFile(uploadPathBodyText, storedNewsletter.getBodyText()); // delete old file
                storedNewsletter.setBodyText(uploadedBodyText);
            }
        }

        return newsletterRepository.save(storedNewsletter);
    }

    /**
     * Upload new newsletter's body HTML file to the server.
     * @param bodyHtml MultipartFile plain/html
     * @return String Uploaded file's name
     */
    private String uploadNewsletterBodyHtml(MultipartFile bodyHtml) {
        return uploads.uploadHtmlFile(uploadPathBodyHtml, bodyHtml);
    }

    /**
     * Upload new newsletter's body plain text file to the server.
     * @param bodyText MultipartFile plain/text
     * @return String Uploaded file's name
     */
    private String uploadNewsletterBodyText(MultipartFile bodyText) {
        return uploads.uploadTextFile(uploadPathBodyText, bodyText);
    }

    /**
     * Download newsletter's HTML body.
     * @param id Long Newsletter's id
     * @return ResponseEntity Resource The stored file.
     * @exception InformationNotFoundException Newsletter not found
     */
    public ResponseEntity<Resource> downloadNewsletterBodyHtml(Long id) {
        Newsletter newsletter = getNewsletterById(id);

        if (newsletter == null) throw new InformationNotFoundException("Newsletter with id " + id + " not found");

        return uploads.downloadFile(uploadPathBodyHtml, newsletter.getBodyHtml());
    }

    /**
     * Download newsletter's Plain Text body.
     * @param id Long Newsletter's id
     * @return ResponseEntity Resource The stored file.
     * @exception InformationNotFoundException Newsletter not found
     */
    public ResponseEntity<Resource> downloadNewsletterBodyText(Long id) {
        Newsletter newsletter = getNewsletterById(id);

        if (newsletter == null) throw new InformationNotFoundException("Newsletter with id " + id + " not found");

        return uploads.downloadFile(uploadPathBodyText, newsletter.getBodyText());
    }

