package com.ga.JNews.controllers;

import com.ga.JNews.models.Subscriber;
import com.ga.JNews.services.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Subscriber subscribe(@RequestBody Subscriber subscriber) {
        return subscriberService.createSubscriber(subscriber);
    }
}
