package ru.practice.notificationservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final EmailSenderService emailSenderService;

    public NotificationService(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @KafkaListener(topics = "user-created", groupId = "my-group")
    public void userCreatedEmail(String email) {
        log.info("Запрос на отправку уведомления о создании аккаунта: {}", email);

        try {
            emailSenderService.sendMessage(email, "Аккаунт создан.", "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.");
        } catch (Exception e) {
            log.error("Уведомление о создании не отправлено на почту: {}. Причина: {}", email, e.toString());
            return;
        }

        log.info("Уведомление о создании отправлено на почту: {}", email);
    }

    @KafkaListener(topics = "user-deleted", groupId = "my-group")
    public void userDeletedEmail(String email) {
        log.info("Запрос на отправку уведомления о удалении аккаунта: {}", email);

        try {
            emailSenderService.sendMessage(email, "Аккаунт удален.", "Здравствуйте! Ваш аккаунт был удалён.");
        } catch (Exception e) {
            log.error("Уведомление о удалении не отправлено на почту: {}. Причина: {}", email, e.toString());
            return;
        }

        log.info("Уведомление о удалении отправлено на почту: {}", email);
    }
}
