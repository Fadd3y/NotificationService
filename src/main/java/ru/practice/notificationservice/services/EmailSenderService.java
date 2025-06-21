package ru.practice.notificationservice.services;

import jakarta.annotation.PostConstruct;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.practice.notificationservice.util.InvalidEmailException;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);
    private final JavaMailSender mailSender;
    private final EmailValidator emailValidator;

    @Value(value = "${spring.mail.username}")
    private String emailFrom;

    public EmailSenderService(JavaMailSender mailSender, EmailValidator emailValidator) {
        this.mailSender = mailSender;
        this.emailValidator = emailValidator;
    }

    public void sendMessage(String toEmail, String subject, String message) {
        if (!emailValidator.isValid(toEmail)) {
            throw new InvalidEmailException("Invalid email: " + toEmail);
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(emailFrom);
        msg.setTo(toEmail);
        msg.setSubject(subject);
        msg.setText(message);

        mailSender.send(msg);

        log.info("Письмо отправлено: {}", msg);
    }
}
