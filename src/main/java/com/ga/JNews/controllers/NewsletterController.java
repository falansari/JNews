package com.ga.JNews.controllers;

import com.ga.JNews.exceptions.BadRequestException;
import com.ga.JNews.exceptions.InformationExistException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.Newsletter;
import com.ga.JNews.services.NewsletterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "newsletters")
public class NewsletterController {
    private final NewsletterService newsletterService;

    @Autowired
    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    /**
     * Get newsletter by their database ID.
     * @param id Long
     * @return Newsletter
     * @exception InformationNotFoundException Doesn't exist
     */
    @GetMapping("/{id}")
    public Newsletter getNewsletterById(@PathVariable("id") Long id) {
        return newsletterService.getNewsletterById(id);
    }

    /**
     * Get newsletter by its title.
     * @param title String
     * @return Newsletter
     * @exception InformationNotFoundException Doesn't exist
     */
    @GetMapping("")
    public Newsletter getNewsletterByTitle(@RequestParam("title") String title) {
        return newsletterService.getNewsletterByTitle(title);
    }

    /**
     * Get all newsletters from database. Asynchronous operation.
     * @return CompletableFuture ArrayList Newsletter
     */
    @GetMapping("/list")
    public CompletableFuture<ArrayList<Newsletter>> getNewsletterList() {
        return newsletterService.getNewsletters();
    }

    /**
     * Download newsletter's body HTML file.
     * @param id Long Newsletter's database ID
     * @return ResponseEntity Resource .html File
     */
    @GetMapping("/{id}/html")
    public ResponseEntity<Resource> getNewsletterHtmlBody(@PathVariable("id") Long id) {
        return newsletterService.downloadNewsletterBodyHtml(id);
    }

    /**
     * Download newsletter's body plain text file.
     * @param id Long Newsletter's database ID
     * @return ResponseEntity Resource .html File
     */
    @GetMapping("/{id}/text")
    public ResponseEntity<Resource> getNewsletterTextBody(@PathVariable("id") Long id) {
        return newsletterService.downloadNewsletterBodyText(id);
    }

    /**
     * Create a new newsletter and save to storage. Accepts form data.
     * @param title String
     * @param subject String
     * @param body_html MultipartFile .html HTML file
     * @param body_text MultipartFile .txt Plain text file
     * @return Newsletter
     * @exception InformationExistException Newsletter title already exists.
     * @exception BadRequestException Missing data, all fields required.
     * @apiNote String title, String subject, String body_html (full .html document), String body_text (plain-text fallback version)
     */
    @PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Newsletter createNewsletter(@RequestParam("title") String title,
                                       @RequestParam("subject") String subject,
                                       @RequestParam("body_html") MultipartFile body_html,
                                       @RequestParam("body_text") MultipartFile body_text) {
        return newsletterService.createNewsletter(title, subject, body_html, body_text);
    }

    /**
     * Update Newsletter's data by its id.
     * @param id Long Existing newsletter's database ID
     * @param title String Newsletter's unique title name. If null or already exists in db will be skipped.
     * @param subject String Newsletter's email subject line. If null will be skipped.
     * @param body_html MultipartFile Newsletter's .html formatted HTML body file. If empty will be skipped.
     * @param body_text MultipartFile Newsletter's .txt plain text body file, used as backup when HTML is rejected. If empty will be skipped.
     * @return Newsletter updated
     * @exception InformationNotFoundException Newsletter ID doesn't exist
     * @apiNote String title, String subject, String body_html (full .html document), String body_text (plain-text fallback version)
     */
    @PatchMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Newsletter updateNewsletter(@PathVariable("id") Long id,
                                       @RequestParam("title") String title,
                                       @RequestParam("subject") String subject,
                                       @RequestParam("body_html") MultipartFile body_html,
                                       @RequestParam("body_text") MultipartFile body_text) {
        return newsletterService.updateNewsletter(id, title, subject, body_html, body_text);
    }

