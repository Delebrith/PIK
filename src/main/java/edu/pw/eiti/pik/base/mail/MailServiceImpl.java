package edu.pw.eiti.pik.base.mail;

import edu.pw.eiti.pik.base.mail.template.TemplateService;
import edu.pw.eiti.pik.user.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;

@Slf4j
@Service
class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final TemplateService templateService;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, TemplateService templateService) {
        this.mailSender = mailSender;
        this.templateService = templateService;
    }

    @Override
    public void send(String to, String subject, String content, boolean html) {
        log.debug("Sending '{}' message to {}", to, subject);
        try {
            mailSender.send(mimeMessage -> {
                final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content, html);
            });
        } catch (final Exception e) {
            log.error("Failed to send message - check configuration", e);
        }
    }

    @TransactionalEventListener
    public void handleEvent(UserCreatedEvent event) {
        send(event.getUser().getEmail(), "Konto w portalu WEiTI Projekty",
                templateService.merge("mail/accountCreated.ftl",
                        Collections.singletonMap("user", event.getUser())), true);
    }
}
