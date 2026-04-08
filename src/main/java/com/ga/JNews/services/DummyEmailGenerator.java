package com.ga.JNews.services;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Auto generates dummy e-mails for testing. MADE WITH COPILOT FOR TESTING PURPOSES ONLY.
 */
public class DummyEmailGenerator {
    public static void main(String[] args) {
        String[] domains = {"gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "example.org"};

        try (FileWriter writer = new FileWriter("dummy_emails.csv")) {
            // Write header
            writer.write("email\n");

            // Generate 1000 emails
            for (int i = 1; i <= 1000; i++) {
                String domain = domains[i % domains.length];
                writer.write("user" + i + "@" + domain + "\n");
            }

            System.out.println("CSV file created successfully: dummy_emails.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
