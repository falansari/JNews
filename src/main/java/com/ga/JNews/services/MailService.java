package com.ga.JNews.services;

import com.ga.JNews.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send new account's verification token e-mail to the new user
     * @param user User
     * @param verificationToken String
     */
    public void sendVerificationMail(User user, String verificationToken) {
        String subject = "JNews New User E-mail Verification Token";
        String url = "http://localhost:8090/auth/users/verify?token=" + verificationToken;
        String message = "Salaam Alaikum " + user.getEmail() + ",\n\n"
                + "Please verify your account by clicking on the link below:\n"
                + url + "\n\n"
                + "This link will expire within 1 day of issuing. You can request a new token if needed." + "\n\n"
                + "Thank you for using JNews <3," + "\n"
                + "JNews Development Team";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    public void sendPasswordResetMail(User user, String token) {
        String subject = "JNews User Password Reset Token";
        String url = "http://localhost:8090/auth/users/reset?token=" + token;
        String message = "Salaam Alaikum " + user.getEmail() + ",\n\n"
                + "You can reset your account password by clicking on the link below:\n"
                + url + "\n\n"
                + "This link will expire within 20 minutes of issuing. You can request a new token if needed." + "\n\n"
                + "Thank you for using JNews <3," + "\n"
                + "JNews Development Team";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }
}
