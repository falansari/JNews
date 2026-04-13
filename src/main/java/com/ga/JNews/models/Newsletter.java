package com.ga.JNews.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "newsletters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Newsletter {
    /**
     * Auto-generated ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title / Name for the template
     */
    @Column(nullable = false, unique = true)
    private String title;

    /**
     * Newsletter's Subject line when sent as e-mail.
     */
    @Column(nullable = false)
    private String subject;

    /**
     * Newsletter's HTML body when sent as e-mail. Supports rich formatting.
     */
    @Lob
    @Column(nullable = false)
    private String bodyHtml;

    /**
     * Newsletter's optional plain-text fallback in case HTML cannot be loaded to subscriber.
     */
    @Lob
    @Column(nullable = false)
    private String bodyText;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
