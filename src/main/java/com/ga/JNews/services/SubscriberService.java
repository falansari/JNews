package com.ga.JNews.services;

import com.ga.JNews.exceptions.BadRequestException;
import com.ga.JNews.exceptions.InformationExistException;
import com.ga.JNews.exceptions.InformationNotFoundException;
import com.ga.JNews.models.Subscriber;
import com.ga.JNews.models.enums.SubscriberStatus;
import com.ga.JNews.repositories.SubscriberRepository;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        if (!emailIsValid(email)) {
            throw new BadRequestException(email + " is not a valid e-mail address.");
        }

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
        if (!emailIsValid(subscriber.getEmail())) {
            throw new BadRequestException(subscriber.getEmail() + " is not a valid e-mail address.");
        }

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

        if (subscriber.getEmail() != null && emailIsValid(subscriber.getEmail())) {
            existingSubscriber.setEmail(subscriber.getEmail());
        }
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
        if (!emailIsValid(email)) {
            throw new BadRequestException(email + " is not a valid e-mail address.");
        }

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

                if (!emailIsValid(email)) {
                    System.out.println("Skipping invalid e-mail: " + email);
                    continue;
                }

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

    /**
     * Validate if an input e-mail is a valid mail address or not.
     * @param email String
     * @return boolean true if valid, false otherwise.
     */
    public static boolean emailIsValid(String email) {
        try {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get list of all subscribers from database. Asynchronous operation.
     * @return CompletableFuture ArrayList Subscriber
     */
    public CompletableFuture<ArrayList<Subscriber>> getSubscribersList() {
        return subscriberRepository.findAllBy();
    }

    /**
     * Export complete Subscribers list to CSV file. Asynchronous operation, supports multithreading.
     * @return CompletableFuture ResponseEntity Resource text/csv
     */
    @Async("executor")
    public CompletableFuture<ResponseEntity<Resource>> exportSubscribersToFile() {
        String folder = "exports/";
        String filename = folder + "subscribers_"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HHmmss")) +".csv";
        CompletableFuture<ArrayList<Subscriber>> subscribers = getSubscribersList();

        File file = new File(filename);
        file.getParentFile().mkdirs();

        return subscribers.thenApplyAsync(list -> {
            lock.writeLock().lock();

            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write("id,email,name,status,created_at,updated_at\n");

                for (Subscriber subscriber : list) {
                    writer.write(
                            subscriber.getId() + ","
                            + subscriber.getEmail() + ","
                            + subscriber.getName() + ","
                            + subscriber.getStatus() + ","
                            + subscriber.getCreatedAt() + ","
                            + subscriber.getUpdatedAt() + "\n"
                    );
                }
            } catch (IOException e) {
                throw new RuntimeException("Error while creating csv file: " + e.getMessage());
            } finally {
                lock.writeLock().unlock();
            }

            Resource resourceFile = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + folder + resourceFile.getFilename())
                    .body(resourceFile);
        });
    }

    /**
     * Creates subscribers and updates existing ones from CSV file with full subscriber info. Multi-threading supported.
     * Skips subscribers that already exist in the database.
     * @param file MultipartFile CSV, plain text. [id,email,name,status]
     * @apiNote IMPORTANT: First row assumed header and skipped.
     * @return List Newly added subscribers, or empty ArrayList if none new.
     */
    @Async("executor")
    public CompletableFuture<ArrayList<Subscriber>> importSubscribersFromFile(MultipartFile file) {
        lock.writeLock().lock();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String line;
            ArrayList<Subscriber> subscriberArrayList = new ArrayList<>();
            bufferedReader.readLine(); // skip first line assumed header

            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(",", -1); // keep trailing empty strings, for rows with missing data points
                Long id = !data[0].isEmpty() ? Long.parseLong(data[0]) :  0;
                String email = !data[1].isEmpty() ? data[1] : "";
                String name = !data[2].isEmpty() ? data[2] : null;
                SubscriberStatus status = !data[3].isEmpty() ? SubscriberStatus.valueOf(data[3]) : SubscriberStatus.SUBSCRIBED;

                if (subscriberRepository.findById(id).isPresent()) { // update existing record
                    Subscriber subscriber = subscriberRepository.findById(id).get();
                    if (emailIsValid(email)) subscriber.setEmail(email);
                    if (name != null) subscriber.setName(name);
                    subscriber.setStatus(status);
                    Subscriber updatedSubscriber = updateSubscriber(id, subscriber);
                    subscriberArrayList.add(updatedSubscriber);

                } else { // add new record
                    if (!emailIsValid(email)) continue; // skip row with invalid email address
                    if (subscriberRepository.existsByEmail(email)) continue; // skip row with existing email in db

                    Subscriber subscriber = new Subscriber();
                    subscriber.setEmail(email);
                    if (name != null) subscriber.setName(name);
                    subscriber.setStatus(status);
                    Subscriber newSubscriber = createSubscriber(subscriber);
                    subscriberArrayList.add(newSubscriber);
                }
            }

            return CompletableFuture.completedFuture(subscriberArrayList);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading csv file: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
}
