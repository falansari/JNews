package com.ga.JNews.services;

import com.ga.JNews.exceptions.InformationExistException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.Subscriber;
import com.ga.JNews.models.enums.SubscriberStatus;
import com.ga.JNews.repositories.SubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

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

    /**
     * Create new subscribers from an email list. Multi-threading supported.
     * Skips subscribers that already exist in the database.
     * @param file MultipartFile CSV, plain text.
     * @return List Newly added subscribers, or empty ArrayList if none new.
     */
    @Async("executor")
    public CompletableFuture<ArrayList<Subscriber>> createSubscribers(MultipartFile file) {
        lock.writeLock().lock();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String email;
            ArrayList<Subscriber> newSubscribersList = new ArrayList<>();

            while ((email = bufferedReader.readLine()) != null) {
                email = email.replace("\uFEFF", "").trim(); // Remove BOM characters from first line in CSV files

                if (subscriberRepository.existsByEmail(email)) continue; // Skip already existing subscriber.

                Subscriber subscriber = new Subscriber();
                subscriber.setEmail(email);
                newSubscribersList.add(createSubscriber(subscriber));
            }

            return CompletableFuture.completedFuture(newSubscribersList);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading csv file: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
}
