package com.ga.JNews.services;

import com.ga.JNews.exceptions.InformationExistException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.Subscriber;
import com.ga.JNews.models.enums.SubscriberStatus;
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
            throw new InformationExistException("Subscriber with email " + subscriber.getEmail() + " already exists");
        }

        return subscriberRepository.save(subscriber);
    }

    /**
     * Update the info (email or name) of a subscriber.
     * @param id Long Subscriber's id
     * @return Subscriber after update
     */
    public Subscriber updateSubscriber(Long id, Subscriber subscriber) {
        Subscriber existingSubscriber = subscriberRepository.findById(id).orElseThrow(() -> new InformationNotFoundException("Subscriber with id " + id + " not found"));

        if (subscriber.getEmail() != null) existingSubscriber.setEmail(subscriber.getEmail());
        if (subscriber.getName() != null) existingSubscriber.setName(subscriber.getName());

        return subscriberRepository.save(existingSubscriber);
    }

    /**
     * Change the status of an existing subscriber.
     * @param email String email
     * @param status SubscriberStatus SUBSCRIBED, UNSUBSCRIBED.
     * @return Subscriber
     */
    public Subscriber setSubscriberStatus(String email, SubscriberStatus status) {
        if (!subscriberRepository.existsByEmail(email)) {
            throw new InformationNotFoundException("Subscriber with email " + email + " not found");
        }

        Subscriber existingSubscriber = subscriberRepository.findByEmail(email);
        existingSubscriber.setStatus(status);
        return subscriberRepository.save(existingSubscriber);
    }

    /**
     * Hard delete a subscriber from the system. Removes all associated mail data as well.
     * @param id Long
     * @return boolean True if successful, else throws an error.
     */
    public boolean deleteSubscriber(Long id) {
        if (!subscriberRepository.existsById(id)) {
            throw new InformationNotFoundException("Subscriber with id " + id + " not found");
        }

        subscriberRepository.deleteById(id);
        return true;
    }
}
