package com.ga.JNews.services;

import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.Subscriber;
import com.ga.JNews.repositories.SubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;

    @Autowired
    public SubscriberService(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    /**
     * Get subscriber by their database ID.
     * @param id Long
     * @return Subscriber
     */
    public Subscriber getSubscriberById(Long id) {
        return subscriberRepository.findById(id).orElseThrow(() -> new InformationNotFoundException("Subscriber with id " + id + " not found"));
    }

    /**
     * Get subscriber by their e-mail address.
     * @param email String
     * @return Subscriber
     */
    public Subscriber getSubscriberByEmail(String email) {
        Subscriber subscriber = subscriberRepository.findByEmail(email);

        if (subscriber == null) {
            throw new InformationNotFoundException("Subscriber with email " + email + " not found");
        }

        return subscriber;
    }

    /**
     * Create a new subscriber.
     * @param subscriber Subscriber
     * @apiNote email, name (optional), status (SUBSCRIBED, UNSUBSCRIBED) (optional)
     * @return Subscriber
     */
    public Subscriber createSubscriber(Subscriber subscriber) {
        if (subscriberRepository.existsByEmail(subscriber.getEmail())) {
            throw new InformationNotFoundException("Subscriber with email " + subscriber.getEmail() + " already exists");
        }

        return subscriberRepository.save(subscriber);
    }
}
