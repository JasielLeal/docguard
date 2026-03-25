package br.com.harmony.DocGuard.infrastructure.email;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender,
                        SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public enum EmailType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        PASSWORD_CHANGED
    }

    public void sendEmail(String to, String name, String link, EmailType type) {

        try {

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("link", link);

            String template;
            String subject;

            switch (type) {
                case EMAIL_VERIFICATION -> {
                    template = "CreateUser";
                    subject = "Verifique sua conta";
                }
                case PASSWORD_RESET -> {
                    template = "ResetPassword";
                    subject = "Recuperação de senha";
                }
                case PASSWORD_CHANGED -> {
                    template = "PasswordChanged";
                    subject = "Senha atualizada";
                }
                default -> throw new IllegalArgumentException("Invalid email type");
            }

            String html = templateEngine.process(template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("contato@revendaja.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }
}