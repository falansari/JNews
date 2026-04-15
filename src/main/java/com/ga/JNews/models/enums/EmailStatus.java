package com.ga.JNews.models.enums;

/**
 * Unopened: Email has been delivered to subscriber's mailbox but not yet opened.
 * Opened: Subscriber opened the newsletter email.
 * Bounced: Email failed to reach the subscriber's mailbox, either due to invalid e-mail or full mailbox.
 */
public enum EmailStatus {
    UNOPENED,
    OPENED,
    BOUNCED
}
