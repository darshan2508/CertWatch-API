package in.certificatemanager.certWatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String fromEmail;

    @Value("${brevo.sender.name}")
    private String fromName;

    private static final String BREVO_URL =
            "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    // ------------------------------------------------------------------
    // Simple HTML Email
    // ------------------------------------------------------------------
    public void sendEmail(String to, String subject, String body) {
        sendEmailInternal(to, subject, body, null, null);
    }

    // ------------------------------------------------------------------
    // Email with Attachment
    // ------------------------------------------------------------------
    public void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String filename
    ) {
        sendEmailInternal(to, subject, body, attachment, filename);
    }

    // ------------------------------------------------------------------
    // Core Brevo API Logic
    // ------------------------------------------------------------------
    private void sendEmailInternal(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String filename
    ) {

        Map<String, Object> payload = new HashMap<>();

        payload.put("sender", Map.of(
                "email", fromEmail,
                "name", fromName
        ));

        payload.put("to", List.of(
                Map.of("email", to)
        ));

        payload.put("subject", subject);
        payload.put("htmlContent", body);

        // Attachment support
        if (attachment != null && filename != null) {
            payload.put("attachment", List.of(
                    Map.of(
                            "name", filename,
                            "content", Base64.getEncoder().encodeToString(attachment)
                    )
            ));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(
                    BREVO_URL,
                    request,
                    String.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email via Brevo", e);
        }
    }
}
//    public void sendEmail(String to, String subject, String body){
//        try{
//            System.out.println("Sending in progress.");
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            System.out.println(fromEmail);
//            message.setTo(to);
//            message.setSubject(subject);
//            message.setText(body);
//            mailSender.send(message);
//            System.out.println("Mail sent.");
//        }catch(Exception e){
//            throw new RuntimeException(e.getMessage());
//        }
//    }
//
//    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String filename) throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//        helper.setFrom(fromEmail);
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(body);
//        helper.addAttachment(filename, new ByteArrayResource(attachment));
//        mailSender.send(message);
//    }

