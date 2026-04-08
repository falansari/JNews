package com.ga.JNews.controllers;

import com.ga.JNews.models.Subscriber;
import com.ga.JNews.models.enums.SubscriberStatus;
import com.ga.JNews.services.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "subscribers")
public class SubscriberController {
    private final SubscriberService subscriberService;

    @Autowired
    public SubscriberController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    /**
     * Get subscriber by their ID.
     * @param id Long
     * @return Subscriber
     */
    @GetMapping("/{id}")
    public Subscriber getSubscriberById(@PathVariable("id") Long id) {
        return subscriberService.getSubscriberById(id);
    }

    /**
     * Get subscriber by their e-mail.
     * @param email String
     * @return Subscriber
     */
    @GetMapping("")
    public Subscriber getSubscriberByEmail(@RequestParam("email") String email) {
        return subscriberService.getSubscriberByEmail(email);
    }

    /**
     * Create a new subscriber.
     * @param subscriber Subscriber
     * @apiNote email, name (optional), status (SUBSCRIBED, UNSUBSCRIBED) (optional)
     * @return Subscriber
     */
    @PostMapping("/subscribe")
    public Subscriber createSubscriber(@RequestBody Subscriber subscriber) {
        return subscriberService.createSubscriber(subscriber);
    }

    /**
     * Update the info (email or name) of a subscriber.
     * @param id Long Subscriber's id
     * @param subscriber Subscriber email and/or name
     * @return Subscriber after update
     */
    @PatchMapping("/{id}")
    public Subscriber updateSubscriber(@PathVariable("id") Long id, @RequestBody Subscriber subscriber) {
        return subscriberService.updateSubscriber(id, subscriber);
    }

    /**
     * Subscribe an existing unsubscribed member.
     * @param email String Subscriber's email
     * @return Subscriber
     */
    @PatchMapping("/subscribe")
    public Subscriber subscribe(@RequestParam("email") String email) {
        return subscriberService.setSubscriberStatus(email, SubscriberStatus.SUBSCRIBED);
    }

    /**
     * Unsubscribe an existing subscribed member.
     * @param email String Subscriber's email
     * @return Subscriber
     */
    @PatchMapping("/unsubscribe")
    public Subscriber unsubscribe(@RequestParam("email") String email) {
        return subscriberService.setSubscriberStatus(email, SubscriberStatus.UNSUBSCRIBED);
    }

    /**
     * Hard delete a subscriber from the system. Deletes all associated mails as well.
     * @param id Long
     * @return boolean true if successful, otherwise throws an error.
     */
    @DeleteMapping("/{id}")
    public boolean deleteSubscriber(@PathVariable("id") Long id) {
        return subscriberService.deleteSubscriber(id);
    }

    /**
     * Create new subscribers from an email list.
     * @param file MultipartFile CSV, plain-text.
     * @apiNote Asynchronous operation, supports multithreading.
     * @return ArrayList Newly added subscribers.
     */
    @PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ArrayList<Subscriber>> createSubscribers(@RequestParam("file") MultipartFile file) {
        return subscriberService.createSubscribers(file);
    }
}
