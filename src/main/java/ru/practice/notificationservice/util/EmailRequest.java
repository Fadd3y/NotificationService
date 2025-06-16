package ru.practice.notificationservice.util;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class EmailRequest {

    @Email(message = "Неверный формат электронной почты. ")
    @NotEmpty(message = "Электронный адрес не заполнен. ")
    @Size(min = 5, max = 256, message = "Размер электронного адреса должен быть от 5 до 256 символов.")
    private String email;

    @NotEmpty(message = "Тема письма не заполнен. ")
    @Size(min = 5, max = 256, message = "Размер темы письма должен быть от 5 до 256 символов.")
    private String subject;

    @NotEmpty(message = "Текст письма не может быть пуст. ")
    @Size(min = 1, max = 10000, message = "Размер темы письма должен быть от 1 до 10000 символов.")
    private String text;

    public EmailRequest(String email, String subject, String text) {
        this.email = email;
        this.subject = subject;
        this.text = text;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "EmailRequest{" +
                "email='" + email + '\'' +
                ", subject='" + subject + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
