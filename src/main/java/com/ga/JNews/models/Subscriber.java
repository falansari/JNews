package com.ga.JNews.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ga.JNews.models.enums.SubscriberStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "subscribers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name; // Defaults to the email before @ sign

    @Column(nullable = false)
    private SubscriberStatus status; // Had just subbed when record was made.

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "subscriber")
    private List<Email> emails;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() { // Default name value if none entered by subscriber
        if (name == null && email != null) {
            name = email.substring(0, email.indexOf("@"));
        }

        if (status == null) status = SubscriberStatus.SUBSCRIBED;
    }
}
