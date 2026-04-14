package com.ga.JNews.models;

import com.ga.JNews.models.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "emails")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Email {
    /**
     * Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The exact time the email was delivered by the service.
     */
    @Column
    private LocalDateTime deliveryTime;

    /**
     * Unopened: Email has been delivered to subscriber's mailbox but not yet opened.
     * Opened: Subscriber opened the newsletter email.
     * Bounced: Email failed to reach the subscriber's mailbox, either due to invalid e-mail or full mailbox.
     */
    @Column
    private EmailStatus status;

    /**
     * The newsletter that was sent in this e-mail.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "newsletter_id", referencedColumnName = "id")
    private Newsletter newsletter;

    /**
     * The email's recipient.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", referencedColumnName = "id")
    private Subscriber subscriber;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = EmailStatus.UNOPENED; // Default Status
    }
}
