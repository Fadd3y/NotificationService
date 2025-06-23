package ru.practice.notificationservice.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.practice.notificationservice.util.EmailRequest;
import ru.practice.notificationservice.services.EmailSenderService;
import ru.practice.notificationservice.util.EmailResponse;

@RestController
@RequestMapping("/api/send-email")
public class EmailController {

    private final Logger log = LoggerFactory.getLogger(EmailController.class);
    private final EmailSenderService emailSenderService;

    public EmailController(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @PostMapping()
    public ResponseEntity<EmailResponse> sendEmail(@RequestBody @Valid EmailRequest request, BindingResult bindingResult) {
        log.info("Поступил запрос на отправку email: {}", request);

        if (bindingResult.hasErrors()) {
            String errors = fieldErrorsToString(bindingResult);
            log.warn("Запрос некорректный и письмо не отправлено: {}", errors);

            EmailResponse response = new EmailResponse("Письмо не отправлено: " + errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        emailSenderService.sendMessage(request.getEmail(), request.getSubject(), request.getText());

        log.info("Письмо отправлено");
        EmailResponse response = new EmailResponse("Письмо отправлено пользователю с email: " + request.getEmail());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<EmailResponse> handleException(Exception e) {
        EmailResponse response = new EmailResponse("Письмо не отправлено - ошибка на сервере: " + e);
        log.error("Ошибка при отправке email: {}", e.toString());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private String fieldErrorsToString(BindingResult bindingResult) {
        StringBuilder builder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder
                    .append(fieldError.getField())
                    .append(" error: ")
                    .append(fieldError.getDefaultMessage())
                    .append("; ");
        }
        return builder.toString();
    }
}
