package com.subscription.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender    mailSender;
    private final TemplateEngine    templateEngine;

    @Value("${notification.mail.from-name}")
    private String fromName;

    @Value("${notification.mail.from-address}")
    private String fromAddress;

    /**
     * Sends a styled HTML renewal reminder email.
     *
     * @param toEmail          recipient email address
     * @param recipientName    user's full name (or email if name unavailable)
     * @param subscriptionName name of the subscription (e.g., "Netflix")
     * @param amount           subscription cost
     * @param currency         currency code (e.g., "USD")
     * @param renewalDate      date of upcoming renewal
     * @param daysUntilRenewal number of days until renewal
     */
    public void sendRenewalReminder(String toEmail, String recipientName,
                                     String subscriptionName, BigDecimal amount,
                                     String currency, LocalDate renewalDate,
                                     long daysUntilRenewal) {

        log.info("Sending renewal reminder to {} for subscription '{}'", toEmail, subscriptionName);

        Context context = new Context();
        context.setVariable("recipientName", recipientName);
        context.setVariable("subscriptionName", subscriptionName);
        context.setVariable("amount", amount);
        context.setVariable("currency", currency);
        context.setVariable("renewalDate", renewalDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        context.setVariable("daysUntilRenewal", daysUntilRenewal);
        context.setVariable("currentYear", LocalDate.now().getYear());

        String htmlContent = templateEngine.process("renewal-reminder", context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject("⏰ Renewal Reminder: " + subscriptionName + " renews in " + daysUntilRenewal + " days");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Renewal reminder sent successfully to {} for '{}'", toEmail, subscriptionName);

        } catch (MessagingException e) {
            log.error("Failed to send renewal reminder to {} for '{}': {}",
                    toEmail, subscriptionName, e.getMessage());
            throw new RuntimeException("Failed to send email to " + toEmail, e);
        } catch (java.io.UnsupportedEncodingException e) {
            log.error("Unsupported encoding for sender name: {}", e.getMessage());
            throw new RuntimeException("Failed to set sender name", e);
        }
    }
}
