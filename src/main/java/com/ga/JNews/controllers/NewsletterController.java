package com.ga.JNews.controllers;

import com.ga.JNews.exceptions.BadRequestException;
import com.ga.JNews.exceptions.InformationExistException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.Newsletter;
import com.ga.JNews.services.NewsletterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
