package ru.practice.notificationservice;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.practice.notificationservice.services.EmailSenderService;
import ru.practice.notificationservice.util.InvalidEmailException;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;


@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class NotificationServiceTest {

    @Container
    private static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0")).withKraft();

    @DynamicPropertySource
    private static void setBootstrapServer(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private GreenMail mail;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoSpyBean
    private EmailSenderService emailSenderService;

    @BeforeEach
    void clearEmails() throws FolderException {
        mail.purgeEmailFromAllMailboxes();
    }

    @Test
    public void testUserCreatedEmail_whenOk() throws MessagingException, IOException {
        String toEmail = "test@test.com";

        kafkaTemplate.send("user-created", toEmail);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = mail.getReceivedMessages();
            assertTrue(mail.getReceivedMessages().length > 0);
        });
        MimeMessage[] messages = mail.getReceivedMessages();
        Message message = messages[0];
        String recipient = message.getRecipients(Message.RecipientType.TO)[0].toString();
        String subject = message.getSubject();
        String text = message.getContent().toString();
        assertEquals(toEmail, recipient);
        assertEquals("Аккаунт создан.", subject);
        assertEquals("Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.", text);
    }

    @Test
    public void testUserCreatedEmail_whenMailException() {
        kafkaTemplate.send("user-created", "test@test.com");

        doThrow(MailSendException.class).when(emailSenderService)
                .sendMessage(any(), any(),any());

        assertThrows(MailException.class, () -> emailSenderService
                .sendMessage(any(), any(),any()));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = mail.getReceivedMessages();
            assertEquals(0, mail.getReceivedMessages().length);
        });
    }

    @Test
    public void testUserCreatedEmail_whenInvalidEmail() {
        kafkaTemplate.send("user-created", "testEmailtest.com");

        assertThrows(InvalidEmailException.class, () -> emailSenderService
                .sendMessage(any(), any(),any()));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = mail.getReceivedMessages();
            assertEquals(0, mail.getReceivedMessages().length);
        });
    }

    @Test
    public void testUserDeletedEmail_whenOk() throws MessagingException, IOException {
        String toEmail = "test@test.com";

        kafkaTemplate.send("user-deleted", toEmail);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = mail.getReceivedMessages();
            assertTrue(mail.getReceivedMessages().length > 0);
        });
        MimeMessage[] messages = mail.getReceivedMessages();
        Message message = messages[0];
        String recipient = message.getRecipients(Message.RecipientType.TO)[0].toString();
        String subject = message.getSubject();
        String text = message.getContent().toString();
        assertEquals(toEmail, recipient);
        assertEquals("Аккаунт удален.", subject);
        assertEquals("Здравствуйте! Ваш аккаунт был удалён.", text);
    }

    @Test
    public void testUserDeletedEmail_whenMailException() {
        kafkaTemplate.send("user-deleted", "test@test.com");

        doThrow(MailSendException.class).when(emailSenderService)
                .sendMessage(any(), any(),any());

        assertThrows(MailException.class, () -> emailSenderService
                .sendMessage(any(), any(),any()));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = mail.getReceivedMessages();
            assertEquals(0, mail.getReceivedMessages().length);
        });
    }

    @Test
    public void testUserDeletedEmail_whenInvalidEmail() {
        kafkaTemplate.send("user-deleted", "testEmailtest.com");

        assertThrows(InvalidEmailException.class, () -> emailSenderService
                .sendMessage(any(), any(),any()));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            MimeMessage[] messages = mail.getReceivedMessages();
            assertEquals(0, mail.getReceivedMessages().length);
        });
    }

    @TestConfiguration
    static class NotificationServiceTestConfig {
        @Value("${spring.mail.port}")
        private int port;

        @Bean(initMethod = "start", destroyMethod = "stop")
        public GreenMail greenMail() {
            System.out.println(port);
            ServerSetup setup = new ServerSetup(port, null, ServerSetup.PROTOCOL_SMTP);
            GreenMail mail = new GreenMail(setup);
            mail.setUser("test@test.com", "Test", "Test");
            return mail;
        }

        @Bean
        public NewTopic userCreatedTopic() {
            return TopicBuilder.name("user-created")
                    .partitions(1)
                    .replicas(1)
                    .build();
        }

        @Bean
        public NewTopic userDeletedTopic() {
            return TopicBuilder.name("user-deleted")
                    .partitions(1)
                    .replicas(1)
                    .build();
        }
    }


}
